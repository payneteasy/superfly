package org.springframework.core.type.classreading;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Type;
import org.springframework.asm.commons.EmptyVisitor;
import org.springframework.core.type.AnnotationMetadata;

import com.payneteasy.superfly.client.classreading.AnnotationAttributesSource;
import com.payneteasy.superfly.client.classreading.AnnotationMetadataHolder;
import com.payneteasy.superfly.client.classreading.MethodAnnotationMetadataSource;

public class MultipleValuesAnnotationMetadataReadingVisitor
		extends ClassMetadataReadingVisitor implements AnnotationMetadata,
		MethodAnnotationMetadataSource {

	private final AnnotationMetadataHolder classAnnotationMetadataHolder = new AnnotationMetadataHolder();
	private final Map<String, AnnotationMetadataHolder> methodAnnotationMetadataHolders = new HashMap<String, AnnotationMetadataHolder>();

	private final ClassLoader classLoader;
	private final boolean visitMethodAnnotations;

	public MultipleValuesAnnotationMetadataReadingVisitor(ClassLoader classLoader,
			boolean visitMethodAnnotations) {
		this.classLoader = classLoader;
		this.visitMethodAnnotations = visitMethodAnnotations;
	}
	
	@Override
	public MethodVisitor visitMethod(int access, final String name, final String desc,
			final String signature, String[] exceptions) {
		MethodVisitor visitor;
		final String methodKey = name + ":" + desc;
		if (!visitMethodAnnotations) {
			visitor = new EmptyVisitor();
		} else {
			visitor = new EmptyVisitor() {
				@Override
				public AnnotationVisitor visitAnnotation(final String methodAnnotDesc, boolean methodAnnotVisible) {
					final String annotationClassName = Type.getType(methodAnnotDesc).getClassName();
					AnnotationMetadataHolder holder = methodAnnotationMetadataHolders.get(methodKey);
					if (holder == null) {
						holder = new AnnotationMetadataHolder();
						methodAnnotationMetadataHolders.put(methodKey, holder);
					}
					return new AnnotationValueExtractingVisitor(annotationClassName, holder);
				}
			};
		}
		return visitor;
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc, boolean visible) {
		final String annotationClassName = Type.getType(desc).getClassName();
		return new AnnotationValueExtractingVisitor(annotationClassName, classAnnotationMetadataHolder);
	}
	
	public Set<String> getAnnotationTypes() {
		return this.classAnnotationMetadataHolder.getAttributesMap().keySet();
	}

	public boolean hasAnnotation(String annotationType) {
		return this.classAnnotationMetadataHolder.getAttributesMap().containsKey(annotationType);
	}

	public Set<String> getMetaAnnotationTypes(String annotationType) {
		return this.classAnnotationMetadataHolder.getMetaAnnotationMap().get(annotationType);
	}

	public boolean hasMetaAnnotation(String metaAnnotationType) {
		Collection<Set<String>> allMetaTypes = this.classAnnotationMetadataHolder.getMetaAnnotationMap().values();
		for (Set<String> metaTypes : allMetaTypes) {
			if (metaTypes.contains(metaAnnotationType)) {
				return true;
			}
		}
		return false;
	}

	public Map<String, Object> getAnnotationAttributes(String annotationType) {
		return this.classAnnotationMetadataHolder.getAttributesMap().get(annotationType);
	}

	@SuppressWarnings("unchecked")
	protected Object convertToArray(List<Object> list) {
		Class elementClass = detectElementClass(list);
		Object array = Array.newInstance(elementClass, list.size());
		for (int i = 0; i < list.size(); i++) {
			Array.set(array, i, list.get(i));
		}
		return array;
	}

	@SuppressWarnings("unchecked")
	private Class detectElementClass(List<Object> list) {
		// TODO: more sophisticated implementation?
		Class result = list.get(0).getClass();
		for (int i = 1; i < list.size(); i++) {
			Object o = list.get(i);
			if (o == null) {
				continue;
			}
			if (o.getClass() != result) {
				result = Object.class;
				break;
			}
		}
		return result;
	}
	
	private final class AnnotationValueExtractingVisitor extends EmptyVisitor {
		private final Map<String, List<Object>> attributes;
		private final String annotationClassName;
		private final AnnotationMetadataHolder annotationMetadataHolder;

		private AnnotationValueExtractingVisitor(String annotationClassName,
				AnnotationMetadataHolder annotationMetadataHolder) {
			this.attributes = new LinkedHashMap<String, List<Object>>();
			this.annotationClassName = annotationClassName;
			this.annotationMetadataHolder = annotationMetadataHolder;
		}
		
		public void visit(String name, Object value) {
			// Explicitly defined annotation attribute value.
			List<Object> existingValues = attributes.get(name);
			if (existingValues == null) {
				existingValues = new ArrayList<Object>();
				attributes.put(name, existingValues);
			}
			existingValues.add(value);
		}

		@SuppressWarnings("unchecked")
		public void visitEnd() {
			try {
				Class annotationClass = classLoader.loadClass(annotationClassName);
				// Check declared default values of attributes in the annotation type.
				Method[] annotationAttributes = annotationClass.getMethods();
				for (int i = 0; i < annotationAttributes.length; i++) {
					Method annotationAttribute = annotationAttributes[i];
					String attributeName = annotationAttribute.getName();
					Object defaultValue = annotationAttribute.getDefaultValue();
					if (defaultValue != null && !attributes.containsKey(attributeName)) {
						attributes.put(attributeName, Collections.singletonList(defaultValue));
					}
				}
				// Register annotations that the annotation type is annotated with.
				Annotation[] metaAnnotations = annotationClass.getAnnotations();
				Set<String> metaAnnotationTypeNames = new HashSet<String>();
				for (Annotation metaAnnotation : metaAnnotations) {
					metaAnnotationTypeNames.add(metaAnnotation.annotationType().getName());
				}
				annotationMetadataHolder.getMetaAnnotationMap().put(annotationClassName, metaAnnotationTypeNames);
			}
			catch (ClassNotFoundException ex) {
				// Class not found - can't determine meta-annotations.
			}
			
			Map<String, Object> attrs = new HashMap<String, Object>();
			for (Entry<String, List<Object>> entry : attributes.entrySet()) {
				String attrName = entry.getKey();
				List<Object> attrValues = entry.getValue();
				Object singleValue;
				if (attrValues.size() > 1) {
					singleValue = convertToArray(attrValues);
				} else {
					singleValue = attrValues.get(0);
				}
				attrs.put(attrName, singleValue);
			}
			
			annotationMetadataHolder.getAttributesMap().put(annotationClassName, attrs);
		}
	}

	public Set<AnnotationAttributesSource> getMethodsAnnotationMetadata() {
		Set<AnnotationAttributesSource> set = new HashSet<AnnotationAttributesSource>();
		for (AnnotationMetadataHolder holder : methodAnnotationMetadataHolders.values()) {
			set.add(createAnnotationAttributesSource(holder));
		}
		return set;
	}

	protected AnnotationAttributesSource createAnnotationAttributesSource(
			final AnnotationMetadataHolder holder) {
		return new AnnotationAttributesSource() {
			
			public Map<String, Object> getAnnotationAttributes(
					String annotationClassName) {
				return holder.getAttributesMap().get(annotationClassName);
			}
		};
	}

}

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
import org.springframework.asm.Type;
import org.springframework.asm.commons.EmptyVisitor;

public class MultipleValuesAnnotationMetadataReadingVisitor extends
		AnnotationMetadataReadingVisitor {
	
	private final Map<String, Map<String, Object>> attributesMap = new LinkedHashMap<String, Map<String, Object>>();

	private final Map<String, Set<String>> metaAnnotationMap = new LinkedHashMap<String, Set<String>>();

	private final ClassLoader classLoader;

	public MultipleValuesAnnotationMetadataReadingVisitor(ClassLoader classLoader) {
		super(classLoader);
		this.classLoader = classLoader;
	}
	
	public AnnotationVisitor visitAnnotation(final String desc, boolean visible) {
		final String className = Type.getType(desc).getClassName();
		final Map<String, List<Object>> attributes = new LinkedHashMap<String, List<Object>>();
		return new EmptyVisitor() {
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
					Class annotationClass = classLoader.loadClass(className);
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
					metaAnnotationMap.put(className, metaAnnotationTypeNames);
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
				
				attributesMap.put(className, attrs);
			}
		};
	}
	
	public Set<String> getAnnotationTypes() {
		return this.attributesMap.keySet();
	}

	public boolean hasAnnotation(String annotationType) {
		return this.attributesMap.containsKey(annotationType);
	}

	public Set<String> getMetaAnnotationTypes(String annotationType) {
		return this.metaAnnotationMap.get(annotationType);
	}

	public boolean hasMetaAnnotation(String metaAnnotationType) {
		Collection<Set<String>> allMetaTypes = this.metaAnnotationMap.values();
		for (Set<String> metaTypes : allMetaTypes) {
			if (metaTypes.contains(metaAnnotationType)) {
				return true;
			}
		}
		return false;
	}

	public Map<String, Object> getAnnotationAttributes(String annotationType) {
		return this.attributesMap.get(annotationType);
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

}

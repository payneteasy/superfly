package org.payneteasy.superfly.client;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.payneteasy.superfly.client.exception.CollectionException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.security.annotation.Secured;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import com.payneteasy.superfly.api.ActionDescription;

public class ScanningActionDescriptionCollector implements
		ActionDescriptionCollector {
	
	protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
	
	private String[] basePackages = new String[0];
	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private String resourcePattern = DEFAULT_RESOURCE_PATTERN;
	private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);
	private Class<? extends Annotation> annotationClass = Secured.class;

	@Required
	public void setBasePackages(String[] basePackages) {
		this.basePackages = basePackages;
	}

	public void setResourcePatternResolver(
			ResourcePatternResolver resourcePatternResolver) {
		this.resourcePatternResolver = resourcePatternResolver;
	}

	public void setResourcePattern(String resourcePattern) {
		this.resourcePattern = resourcePattern;
	}

	public void setMetadataReaderFactory(MetadataReaderFactory metadataReaderFactory) {
		this.metadataReaderFactory = metadataReaderFactory;
	}

	public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
		this.annotationClass = annotationClass;
	}

	public List<ActionDescription> collect() throws CollectionException {
		Set<String> names = new HashSet<String>();
		for (String basePackage : basePackages) {
			try {
				processPackage(basePackage, names);
			} catch (IOException e) {
				throw new CollectionException(e);
			}
		}
		return buildDescriptions(names);
	}

	protected void processPackage(String basePackage, Set<String> names)
			throws IOException {
		String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
				resolveBasePackage(basePackage) + "/" + this.resourcePattern;
		Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
		for (Resource resource : resources) {
			if (resource.isReadable()) {
				MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
				Map<String, Object> attributes = metadataReader
						.getAnnotationMetadata()
						.getAnnotationAttributes(annotationClass.getName());
				if (attributes != null) {
					Object value = attributes.get("value");
					if (value != null) {
						String[] values;
						if (value instanceof String[]) {
							values = (String[]) value;
						} else if (value instanceof String) {
							values = new String[]{(String) value};
						} else {
							values = new String[]{value.toString()};
						}
						for (String v : values) {
							names.add(v);
						}
					}
				}
			}
		}
	}
	
	protected String resolveBasePackage(String basePackage) {
		return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
	}
	
	protected List<ActionDescription> buildDescriptions(Set<String> names) {
		List<ActionDescription> descriptions = new ArrayList<ActionDescription>(names.size());
		for (String name : names) {
			descriptions.add(new ActionDescription(name));
		}
		return descriptions;
	}
}

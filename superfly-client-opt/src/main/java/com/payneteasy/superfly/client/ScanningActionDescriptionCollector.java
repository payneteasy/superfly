package com.payneteasy.superfly.client;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.client.classreading.AnnotationAttributesSource;
import com.payneteasy.superfly.client.classreading.MethodReadingMetadataReader;
import com.payneteasy.superfly.client.classreading.MethodReadingMetadataReaderFactory;
import com.payneteasy.superfly.client.exception.CollectionException;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ActionDescriptionCollector implementation which scans classes in the given
 * packages looking for a given annotation (by default @Secured) and extracts
 * action names from its attributes.
 * Descriptions of the resulting actions are null.
 *
 * @author Roman Puchkovskiy
 */
public class ScanningActionDescriptionCollector implements
        ActionDescriptionCollector {

    private static final Logger logger = LoggerFactory.getLogger(ScanningActionDescriptionCollector.class);

    protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    private String[] basePackages = new String[0];
    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private String resourcePattern = DEFAULT_RESOURCE_PATTERN;
    private Class<? extends Annotation> annotationClass = Secured.class;
    private ValuesExtractor valuesExtractor = new SecuredValuesExtractor();
    private Set<String> notCollectedActions = Collections.singleton("action_temp_password");

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

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public void setValuesExtractor(ValuesExtractor valuesExtractor) {
        this.valuesExtractor = valuesExtractor;
    }

    public void setNotCollectedActions(Set<String> notCollectedActions) {
        this.notCollectedActions = notCollectedActions;
    }

    public List<ActionDescription> collect() throws CollectionException {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(basePackages)
                .filterInputsBy(new FilterBuilder().includePackage(appendDots(basePackages)))
                .addScanners(new MethodAnnotationsScanner())
        );
        Set<String> extractedActionNames = new HashSet<>();

        final Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(annotationClass);
        for (Class<?> clazz : annotatedClasses) {
            final Annotation annotation = clazz.getAnnotation(annotationClass);
            Assert.notNull(annotation);
            Collections.addAll(extractedActionNames, valuesExtractor.extract(annotation));
        }

        final Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(annotationClass);
        for (Method method : annotatedMethods) {
            final Annotation annotation = method.getAnnotation(annotationClass);
            Assert.notNull(annotation);
            Collections.addAll(extractedActionNames, valuesExtractor.extract(annotation));
        }

        return buildDescriptions(extractedActionNames);
    }

    private String[] appendDots(String[] basePackages) {
        String[] result = new String[basePackages.length];
        for (int i = 0; i < basePackages.length; i++) {
            String packageName = basePackages[i];
            String dottedName = packageName;
            if (StringUtils.hasText(packageName) && !packageName.endsWith(".")) {
                dottedName += ".";
            }
            result[i] = dottedName;
        }
        return result;
    }

    protected void processPackage(String basePackage, Set<String> names,
            MethodReadingMetadataReaderFactory metadataReaderFactory)
            throws IOException {
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(basePackage) + "/" + this.resourcePattern;
        Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MethodReadingMetadataReader metadataReader = metadataReaderFactory.getMethodReadingMetadataReader(
                        resource);

                // class annotations
                Map<String, Object> attributes = metadataReader
                        .getAnnotationMetadata()
                        .getAnnotationAttributes(annotationClass.getName());
                processAnnotationAttributes(names, attributes);

                // method annotations
                Set<AnnotationAttributesSource> methodsAnnotationMetadata = metadataReader.getMethodAnnotationMetadataSource().getMethodsAnnotationMetadata();
                for (AnnotationAttributesSource attributesSource : methodsAnnotationMetadata) {
                    attributes = attributesSource.getAnnotationAttributes(annotationClass.getName());
                    processAnnotationAttributes(names, attributes);
                }
            }
        }
    }

    protected void processAnnotationAttributes(Set<String> names,
            Map<String, Object> attributes) {
        if (attributes != null) {
            Object value = attributes.get(null);
            if (value != null) {
//				String[] values = valuesExtractor.extract(value);
//				Collections.addAll(names, values);
            }
        }
    }

    protected String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }

    protected List<ActionDescription> buildDescriptions(Set<String> names) {
        List<ActionDescription> descriptions = new ArrayList<>(names.size());
        for (String name : names) {
            if (!notCollectedActions.contains(name.toLowerCase())) {
                descriptions.add(new ActionDescription(name));
            }
        }
        return descriptions;
    }
}

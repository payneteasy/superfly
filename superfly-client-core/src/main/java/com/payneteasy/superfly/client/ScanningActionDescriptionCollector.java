package com.payneteasy.superfly.client;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.client.exception.CollectionException;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * ActionDescriptionCollector implementation which scans classes in the given
 * packages looking for a given annotation (by default @Secured) and extracts
 * action names from its attributes.
 * Descriptions of the resulting actions are null.
 *
 * @author Roman Puchkovskiy
 */
public class ScanningActionDescriptionCollector implements ActionDescriptionCollector {

    private static final Logger logger = LoggerFactory.getLogger(ScanningActionDescriptionCollector.class);

    protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    private String[] basePackages = new String[0];
    private Class<? extends Annotation> annotationClass;
    private ValuesExtractor valuesExtractor = new ValueAttributeValuesExtractor();
    private Set<String> notCollectedActions = Collections.singleton("action_temp_password");

    public void setBasePackages(String[] basePackages) {
        this.basePackages = basePackages != null ? basePackages : new String[0];
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public void setValuesExtractor(ValuesExtractor valuesExtractor) {
        this.valuesExtractor = valuesExtractor;
    }

    public void setNotCollectedActions(Set<String> notCollectedActions) {
        this.notCollectedActions = notCollectedActions != null ? notCollectedActions : Collections.emptySet();
    }

    @Override
    public List<ActionDescription> collect() throws CollectionException {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(basePackages)
                .filterInputsBy(new FilterBuilder().includePackage(appendDots(basePackages)))
                .addScanners(new MethodAnnotationsScanner())
        );
        Set<String> extractedActionNames = new HashSet<>();

        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(annotationClass);
        for (Class<?> clazz : annotatedClasses) {
            Annotation annotation = clazz.getAnnotation(annotationClass);
            Objects.requireNonNull(annotation, "annotation cannot be null");
            Collections.addAll(extractedActionNames, valuesExtractor.extract(annotation));
        }

        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(annotationClass);
        for (Method method : annotatedMethods) {
            Annotation annotation = method.getAnnotation(annotationClass);
            Objects.requireNonNull(annotation, "annotation cannot be null");
            Collections.addAll(extractedActionNames, valuesExtractor.extract(annotation));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Collected the following actions: {}", extractedActionNames);
        }
        return buildDescriptions(extractedActionNames);
    }

    private String[] appendDots(String[] basePackages) {
        String[] result = new String[basePackages.length];
        for (int i = 0; i < basePackages.length; i++) {
            String packageName = basePackages[i];
            String dottedName = packageName;
            if (packageName != null && !packageName.isBlank() && !packageName.endsWith(".")) {
                dottedName = packageName + ".";
            }
            result[i] = dottedName;
        }
        return result;
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

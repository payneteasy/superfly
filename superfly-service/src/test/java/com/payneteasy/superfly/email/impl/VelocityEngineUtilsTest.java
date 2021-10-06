package com.payneteasy.superfly.email.impl;

import com.payneteasy.superfly.spring.velocity.VelocityEngineFactory;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author dvponomarev, 05.10.2021
 */
public class VelocityEngineUtilsTest {

    @Test
    public void smokeTest() throws Exception {
        final Map<String, Object> props = new HashMap<>();
        props.put("resource.loaders", "file");
        props.put("resource.loader.file.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        final VelocityEngineFactory factory = new VelocityEngineFactory();
        factory.setVelocityPropertiesMap(props);
        final VelocityEngine velocityEngine = factory.createVelocityEngine();

        final String result = VelocityEngineUtils.mergeTemplateIntoString(
                velocityEngine, "velocity/test.vm", StandardCharsets.UTF_8.toString(), Collections.emptyMap()
        );
        assertEquals("Test vm", result);
    }

}
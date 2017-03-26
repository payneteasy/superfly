package com.payneteasy.superfly.client.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class CommonUtils {
    public static final String CLASSPATH_PREFIX = "classpath:";

    public static Properties loadPropertiesThrowing(String propsLocation) {
        InputStream propsIS = null;
        Properties properties;
        try {
            if (propsLocation.startsWith(CLASSPATH_PREFIX)) {
                propsIS = getInputStreamFromClasspath(propsLocation);
            } else {
                propsIS = getInputStreamFromFile(propsLocation);
            }
            if (propsIS == null) {
                throw new IllegalStateException("Null properties stream: please check whether resource exists: " + propsLocation);
            }
            properties = new Properties();
            properties.load(propsIS);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load properties from the following location: " + propsLocation, e);
        } finally {
            if (propsIS != null) {
                try {
                    propsIS.close();
                } catch (IOException e) {
                    // ignoring
                }
            }
        }
        return properties;
    }

    public static InputStream getInputStreamFromClasspath(String resourceLocation) {
        InputStream propsIS;
        String location = resourceLocation.substring(CLASSPATH_PREFIX.length());
        propsIS = CommonUtils.class.getClassLoader().getResourceAsStream(location);
        return propsIS;
    }

    public static InputStream getInputStreamFromFile(String filename)
            throws FileNotFoundException {
        InputStream propsIS;
        propsIS = new FileInputStream(filename);
        return propsIS;
    }

    public static URL getResourceUrl(String location) throws MalformedURLException {
        if (location.startsWith(CLASSPATH_PREFIX)) {
            String loc = location.substring(CLASSPATH_PREFIX.length());
            return getClasspathResourceUrl(loc);
        } else {
            return getFileResourceUrl(location);
        }
    }

    public static URL getClasspathResourceUrl(String loc) {
        return CommonUtils.class.getClassLoader().getResource(loc);
    }

    public static URL getFileResourceUrl(String location) throws MalformedURLException {
        return new File(location).toURI().toURL();
    }
}

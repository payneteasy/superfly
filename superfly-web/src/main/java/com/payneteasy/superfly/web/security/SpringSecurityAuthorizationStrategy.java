package com.payneteasy.superfly.web.security;

import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.util.io.IOUtils;
import org.springframework.security.access.annotation.Secured;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

public class SpringSecurityAuthorizationStrategy implements IAuthorizationStrategy {

    private static final String DEV_FILE_PATH = "src/main/resources/components-security.properties";
    private static final String PROD_FILE_PATH = "components-security.properties";

    /**
     * Used only in production mode (i.e. when properties are loaded from the classpath).
     */
    private Properties cachedProperties = null;

    @Override
    public <T extends IRequestableComponent> boolean isInstantiationAuthorized(Class<T> componentClass) {
        return true;
    }

    @Override
    public boolean isActionAuthorized(Component component, Action action) {
        boolean ret ;
        String path = component.getPage().getClass().getSimpleName()+"."+component.getId()+"."+action.getName();
        String superPath = component.getPage().getClass().getSuperclass().getSimpleName()+"."+component.getId()+"."+action.getName();

        Properties securityProperties = loadSecurityProperties();
        if(isInSecurityPath(path, securityProperties)) {
            ret = isComponentAllowed(path, securityProperties);
        } else if(isInSecurityPath(superPath, securityProperties)) {
            ret = isComponentAllowed(superPath, securityProperties);
        } else if (component.getClass().isAnnotationPresent(Secured.class)) {
            ret = SecurityUtils.isComponentVisible(component.getClass());
        } else if (component instanceof BookmarkablePageLink<?>) {
            BookmarkablePageLink<?> link = (BookmarkablePageLink<?>) component;
            ret = SecurityUtils.isComponentVisible(link.getPageClass());
        } else {
            ret = true;
        }
        return ret;
    }

    @Override
    public boolean isResourceAuthorized(IResource resource, PageParameters parameters) {
        return true;
    }

    private boolean isComponentAllowed(String aPath, Properties aProps) {
        String line = aProps.getProperty(aPath);
        StringTokenizer st = new StringTokenizer(line, "; , \t");
        while(st.hasMoreTokens()) {
            if(SecurityUtils.isUserInRole(st.nextToken())) {
                return true;
            }
        }
        return false ;
    }

    private boolean isInSecurityPath(String aPath, Properties aProps) {
        return aProps.containsKey(aPath);
    }

    private Properties loadSecurityProperties() {
        Properties prop = new Properties();
        String file = DEV_FILE_PATH;
        boolean devMode = new File(file).exists();
        InputStream in = null;
        try {
            if (devMode) {
                in = new FileInputStream(file);
            } else {
                if (cachedProperties != null) {
                    return cachedProperties;
                }
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl == null) {
                    cl = getClass().getClassLoader();
                }
                in = cl.getResourceAsStream(PROD_FILE_PATH);
            }
            prop.load(in);
            if (!devMode) {
                cachedProperties = prop;
            }
            return prop;
        } catch (IOException e) {
            throw new IllegalStateException("Error reading from file " +file+": "+e.getMessage());
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}

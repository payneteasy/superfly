package com.payneteasy.superfly.web.wicket.page;

import com.payneteasy.superfly.web.wicket.page.sso.Tester;
import junit.framework.TestCase;
import org.apache.wicket.injection.ComponentInjector;
import org.apache.wicket.injection.ConfigurableInjector;
import org.apache.wicket.injection.IFieldValueFactory;
import org.apache.wicket.injection.web.InjectorHolder;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * @author rpuch
 */
public abstract class AbstractPageTest extends TestCase {
    protected Tester tester;

    static {
        Locale.setDefault(Locale.ENGLISH);
    }

    public void setUp() {
        tester = new Tester();
        tester.getApplication().addComponentInstantiationListener(new ComponentInjector() {{
            InjectorHolder.setInjector(createInjector());
        }});
    }

    protected Object getBean(Class<?> type) {
        return null;
    }

    private ConfigurableInjector createInjector() {
        return new ConfigurableInjector() {
            @Override
            protected IFieldValueFactory getFieldValueFactory() {
                return createFieldValueFactory();
            }
        };
    }

    private IFieldValueFactory createFieldValueFactory() {
        return new IFieldValueFactory() {
            @Override
            public Object getFieldValue(Field field, Object fieldOwner) {
                Object result = getBean(field.getType());
                if (result == null) {
                    throw new IllegalStateException("Cannot instantiate " + field.getType());
                }
                return result;
            }

            @Override
            public boolean supportsField(Field field) {
                return getBean(field.getType()) != null;
            }
        };
    }
}

package com.payneteasy.superfly.web.wicket.page;

import com.payneteasy.superfly.web.wicket.page.sso.Tester;
import org.apache.wicket.Component;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.injection.IFieldValueFactory;
import org.apache.wicket.injection.Injector;
import org.junit.Before;
import org.junit.BeforeClass;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * @author rpuch
 */
public abstract class AbstractPageTest {
    protected Tester tester;

    @BeforeClass
    public static void staticInit() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Before
    public void initPageTest() {
        tester = new Tester();
        tester.getApplication().getComponentInstantiationListeners().add(new TestInjector());
    }

    protected Object getBean(Class<?> type) {
        return null;
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

    private class TestInjector extends Injector implements IComponentInstantiationListener {
        private IFieldValueFactory fieldValueFactory = createFieldValueFactory();

        @Override
        public void onInstantiation(Component component) {
            inject(component);
        }

        @Override
        public void inject(Object object) {
            inject(object, fieldValueFactory);
        }
    }
}

package com.payneteasy.superfly.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;

import com.payneteasy.superfly.spisupport.ObjectResolver;

/**
 * {@link ObjectResolver} implementation which uses a {@link BeanFactory} to
 * resolve objects.
 *
 * @author Roman Puchkovskiy
 */
public class BeanFactoryObjectResolver implements ObjectResolver {
	
	private ListableBeanFactory beanFactory;

	public BeanFactoryObjectResolver(ListableBeanFactory beanFactory) {
		super();
		this.beanFactory = beanFactory;
	}

	@SuppressWarnings("unchecked")
	public <T> T resolve(Class<T> clazz) {
		String name = getBeanNameOfClass(beanFactory, clazz);
		if (name == null) {
			return null;
		} else {
			return (T) beanFactory.getBean(name);
		}
	}
	
	private final String getBeanNameOfClass(ListableBeanFactory bf,
			Class<?> clazz) {
		// get the list of all possible matching beans
		List<String> names = new ArrayList<String>(Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(bf, clazz)));
		Iterator<String> it = names.iterator();

		// filter out beans that are not condidates for autowiring
		while (it.hasNext()) {
			final String possibility = it.next();
			if (BeanFactoryUtils.isFactoryDereference(possibility)
					|| possibility.startsWith("scopedTarget.")) {
				it.remove();
			}
		}

		if (names.isEmpty()) {
//			throw new IllegalStateException("bean of type [" + clazz.getName()
//					+ "] not found");
			return null;
		} else if (names.size() > 1) {
			StringBuilder msg = new StringBuilder();
			msg.append("more then one bean of type [");
			msg.append(clazz.getName());
			msg.append("] found, you have to specify the name of the bean ");
			msg
					.append("(@SpringBean(name=\"foo\")) in order to resolve this conflict. ");
			msg.append("Matched beans: ");
			boolean first = true;
			for (String name : names) {
				if (!first) {
					msg.append(",");
				}
				msg.append(name);
				first = false;
			}
			throw new IllegalStateException(msg.toString());
		} else {
			return names.get(0);
		}
	}

}

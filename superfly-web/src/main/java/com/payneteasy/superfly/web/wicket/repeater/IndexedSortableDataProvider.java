package com.payneteasy.superfly.web.wicket.repeater;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Base for a sortable data provider which uses field indices to select a field
 * by which a sorting is made. Fields are indexed from 1.
 * 
 * @author Roman Puchkovskiy
 * @param <T>	provided type
 */
public abstract class IndexedSortableDataProvider<T extends Serializable> extends SortableDataProvider<T> {
	private Map<String, Integer> fieldNameToIndex;
	
	/**
	 * Constructs an instance initializing a mapping between field indices and
	 * their names.
	 * 
	 * @param sortFields	names of fields in the proper order (they will get
	 * 						indexes beginning with 1)
	 */
	public IndexedSortableDataProvider(String[] sortFields) {
		super();
		fieldNameToIndex = new HashMap<String, Integer>();
		for (int i = 0; i < sortFields.length; i++) {
			fieldNameToIndex.put(sortFields[i], i + 1);
		}
		setSort(sortFields[0], true);
	}

	/**
	 * @see SortableDataProvider#model(Object)
	 */
	public IModel<T> model(T object) {
		return new Model<T>(object);
	}

	/**
	 * Returns an index of the currently selected sort field.
	 * 
	 * @return field index (beginning with 1) or 1 if not found in mapping
	 */
	protected int getSortFieldIndex() {
		SortParam sp = getSort();
		Integer index = fieldNameToIndex.get(sp.getProperty());
		if (index == null) {
			index = 1;
		}
		return index;
	}
	
	/**
	 * Returns true if data is currently sorted ascendingly.
	 * 
	 * @return true if ascending, else false
	 */
	protected boolean isAscending() {
		return getSort().isAscending();
	}

}

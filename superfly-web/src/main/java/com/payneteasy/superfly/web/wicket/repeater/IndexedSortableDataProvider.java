package com.payneteasy.superfly.web.wicket.repeater;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.payneteasy.superfly.model.ui.group.UIGroupForList;

/**
 * Base for a sortable data provider.
 * 
 * @author Roman Puchkovskiy
 * @param <T>	provided type
 */
public abstract class IndexedSortableDataProvider<T extends Serializable> extends SortableDataProvider<T> {
	private Map<String, Integer> fieldNameToIndex;
	
	private List<T> dataset=new ArrayList<T>();
	
	public List<T> getDataset() {
		return dataset;
	}

	public void setDataset(List<T> dataset) {
		this.dataset.clear();
		this.dataset.addAll(dataset);
	}

	public IndexedSortableDataProvider(String[] sortFields) {
		super();
		fieldNameToIndex = new HashMap<String, Integer>();
		for (int i = 0; i < sortFields.length; i++) {
			fieldNameToIndex.put(sortFields[i], i + 1);
		}
		setSort(sortFields[0], true);
	}

	public IModel<T> model(T object) {
		return new Model<T>(object);
	}
	
	protected int getSortFieldIndex() {
		SortParam sp = getSort();
		Integer index = fieldNameToIndex.get(sp.getProperty());
		if (index == null) {
			index = 1;
		}
		return index;
	}
	
	protected boolean isAscending() {
		return getSort().isAscending();
	}

}

package com.payneteasy.superfly.web.wicket.component;

import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;

/**
 * DataView which by default uses paging.
 * 
 * @author Roman Puchkovskiy
 * @param <T>
 */
public abstract class PagingDataView<T> extends DataView<T> {

    private static final int DEFAULT_PAGE_SIZE = 30;

    protected PagingDataView(String id, IDataProvider<T> dataProvider) {
        this(id, dataProvider, DEFAULT_PAGE_SIZE);
    }

    public PagingDataView(String id, IDataProvider<T> dataProvider, int itemsPerPage) {
        super(id, dataProvider, itemsPerPage);
    }
}

package com.payneteasy.superfly.web.wicket.component.mapping;

import com.payneteasy.superfly.service.mapping.MappingService;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class MappingPanel<T extends MappingService> extends Panel {

    protected MappingPanel(String id, final long entityId) {
        super(id);
        final MappingModel<MappingService> mappingModel = new MappingModel<>();
        Form<MappingModel<MappingService>> form = new Form<>("form-mapping",
                new Model<>(mappingModel));
        add(form);

        final CheckGroup<MappingService> checkGroupMapped = new CheckGroup<>("checkgroup-mapped",
                mappingModel.getSelectedInMapped());
        form.add(checkGroupMapped);
        checkGroupMapped.add(new CheckGroupSelector("master-checkbox-map", checkGroupMapped));
        checkGroupMapped.add(new Label("mapped-item-name", getHeaderItemName()));

        final IModel<List<T>> mappedListModel = new LoadableDetachableModel<List<T>>() {
            @Override
            public List<T> load() {
                return getMappedItems(mappingModel.getSearchMappedString());
            }
        };
        ListView<T> mappedListView = new ListView<T>("mapped-list", mappedListModel) {

            @Override
            protected void populateItem(ListItem<T> item) {
                T mapped = item.getModelObject();
                item.add(new Label("mapped-name", createObjectNameModel(mapped.getItemName())));
                item.add(new Check<>("selected", new Model<>(mapped)));
            }

        };
        mappedListView.setReuseItems(true);
        checkGroupMapped.add(mappedListView);
        form.add(new SubmitLink("remove-items") {

            @Override
            public void onSubmit() {
                mappingProcess(entityId, null, objectsToIds(checkGroupMapped.getModelObject()));
            }

        });

        final CheckGroup<MappingService> checkGroupUnMapped = new CheckGroup<>("checkgroup-unmapped",
                mappingModel.getSelectedInUnMapped());
        form.add(checkGroupUnMapped);
        checkGroupUnMapped.add(new CheckGroupSelector("master-checkbox-unmap", checkGroupUnMapped));
        checkGroupUnMapped.add(new Label("unmapped-item-name", getHeaderItemName()));

        final IModel<List<T>> unmappedListModel = new LoadableDetachableModel<List<T>>() {
            @Override
            public List<T> load() {
                return getUnMappedItems(mappingModel.getSearchUnMappedString());
            }
        };
        ListView<T> unmappedListView = new ListView<T>("unmapped-list", unmappedListModel) {
            @Override
            protected void populateItem(ListItem<T> item) {
                MappingService unmapped = item.getModelObject();
                item.add(new Label("mapped-name", createObjectNameModel(unmapped.getItemName())));
                item.add(new Check<>("selected", new Model<>(unmapped)));
            }
        };
        unmappedListView.setReuseItems(true);
        checkGroupUnMapped.add(unmappedListView);

        form.add(new SubmitLink("add-items") {
            @Override
            public void onSubmit() {
                mappingProcess(entityId, objectsToIds(checkGroupUnMapped.getModelObject()), null);
            }

        });

    }

    protected abstract List<T> getMappedItems(String searchLabel);

    protected abstract List<T> getUnMappedItems(String searchLabel);

    protected abstract void mappingProcess(long entityId, List<Long> mappedId, List<Long> unmappedId);

    protected abstract String getHeaderItemName();

    private IModel<String> createObjectNameModel(String itemName) {
        return new Model<>(itemName);
    }
    
    private List<Long> objectsToIds(Collection<? extends MappingService> objects) {
        List<Long> ids = new ArrayList<>(objects.size());
        for (MappingService object : objects) {
            ids.add(object.getItemId());
        }
        return ids;
    }
}

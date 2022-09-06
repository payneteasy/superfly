package com.payneteasy.superfly.web.wicket.page.action;

import com.payneteasy.superfly.model.ui.action.UIAction;
import com.payneteasy.superfly.model.ui.action.UIActionForFilter;
import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.service.ActionService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.component.paging.SuperflyPagingNavigator;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.repeater.IndexedSortableDataProvider;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.Strings;
import org.springframework.security.access.annotation.Secured;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Secured("ROLE_ADMIN")
public class CopyActionPropertiesPage extends BasePage {
    @SpringBean
    private ActionService actionService;

    public CopyActionPropertiesPage(final PageParameters parameters) {
        super(ListActionsPage.class, parameters);
        final long actionId = parameters.get("id").toLong(-1);

        // modalWindow
        final ModalWindow modalWindow;
        add(modalWindow = new ModalWindow("modalWindow"));
//        modalWindow.setPageMapName("modal-name");
        modalWindow.setCookieName("modal-cookie");
        modalWindow.setPageCreator(new ModalWindow.PageCreator() {
            public Page createPage() {
                return new CopyActionWindow(CopyActionPropertiesPage.this, modalWindow, parameters);
            }
        });
        modalWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
            public void onClose(AjaxRequestTarget target) {
                if (!parameters.get("copy").isNull()) {
                    setResponsePage(ListActionsPage.class);
                }

            }
        });
        modalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
            public boolean onCloseButtonClicked(AjaxRequestTarget target) {
                return true;
            }
        });

        final ActionFilter actionFilter = new ActionFilter();
        Form<ActionFilter> filtersForm = new Form<ActionFilter>("filters-form");
        add(filtersForm);
        final AutoCompleteTextField<String> autoTextNameAction = new AutoCompleteTextField<String>("auto", new Model<>("")) {

            @Override
            protected Iterator<String> getChoices(String input) {
                if (Strings.isEmpty(input)) {
                    return Collections.<String>emptyList().iterator();
                }
                List<String> choices = new ArrayList<String>(20);
                List<UIActionForFilter> action = actionService.getActionForFilter();
                for (UIActionForFilter uia : action) {
                    final String name = uia.getActionName();
                    if (name.toUpperCase().startsWith(input.toUpperCase())) {
                        choices.add(name);

                        if (choices.size() == 20) {
                            break;
                        }
                    }
                }
                return choices.iterator();
            }
        };
        filtersForm.add(autoTextNameAction);

        UIAction action = actionService.getAction(actionId);
        final long subId = action.getSubsystemId();
        filtersForm.add(new Label("name-action", action == null ? null : action.getActionName()));
        filtersForm.add(new Label("name-description", action == null ? null : action.getActionDescription()));
        filtersForm.add(new Label("subname-action", action == null ? null : action.getSubsystemName()));

        String[] fieldName = { "actionId", "actionName", "actionDescription" };
        SortableDataProvider<UIActionForList, String> actionDataProvider = new IndexedSortableDataProvider<UIActionForList>(fieldName) {

            public Iterator<? extends UIActionForList> iterator(long first, long count) {
                String actionForFilter = autoTextNameAction.getModelObject();
                List<Long> subsystemId = new ArrayList<Long>();
                subsystemId.add(subId);
                List<UIActionForList> actions = actionService.getActions(
                        first,
                        count,
                        getSortFieldIndex(),
                        isAscending(),
                        actionForFilter,
                        null,
                        subsystemId,
                        false
                );
                return actions.iterator();
            }

            public long size() {
                List<Long> subsystemId = new ArrayList<Long>();
                subsystemId.add(subId);
                return actionService.getActionCount(null, null, subsystemId);

            }

        };
        final DataView<UIActionForList> actionDataView = new PagingDataView<UIActionForList>("actionList", actionDataProvider) {

            @Override
            protected void populateItem(Item<UIActionForList> item) {
                final UIActionForList action = item.getModelObject();
                AjaxLink selectActionForCopy = new AjaxLink("select-action") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        parameters.set("copyId", action.getId());
                        modalWindow.show(target);
                    }

                };
                selectActionForCopy.add(new Label("action-name", action.getName()));
                item.add(selectActionForCopy);
                item.add(new Label("action-description", action.getDescription()));
            }

        };
        filtersForm.add(actionDataView);
        filtersForm.add(new OrderByLink<>("order-by-actionName", "actionName", actionDataProvider));
        filtersForm.add(new OrderByLink<>("order-by-actionDescription", "actionDescription", actionDataProvider));
        filtersForm.add(new SuperflyPagingNavigator("paging-navigator", actionDataView));
        filtersForm.add(new BookmarkablePageLink<Page>("back", ListActionsPage.class));

    }

    @SuppressWarnings("unused")
    private class ActionFilter implements Serializable {
        private UIActionForFilter actionForFilter;
        private long actionId;

        public long getActionId() {
            return actionId;
        }

        public void setActionId(long actionId) {
            this.actionId = actionId;
        }

        public UIActionForFilter getActionForFilter() {
            return actionForFilter;
        }

        public void setActionForFilter(UIActionForFilter actionForFilter) {
            this.actionForFilter = actionForFilter;
        }
    }

    @Override
    protected String getTitle() {
        return "Copy action";
    }

}

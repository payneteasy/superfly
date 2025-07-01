package com.payneteasy.superfly.web.wicket.page.action;

import com.payneteasy.superfly.model.ui.action.UIAction;
import com.payneteasy.superfly.model.ui.action.UIActionForFilter;
import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.service.ActionService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.component.paging.SuperflyPagingNavigator;
import com.payneteasy.superfly.web.wicket.component.userActions.CopyActionWindow;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.repeater.IndexedSortableDataProvider;
import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalDialog;
import org.apache.wicket.extensions.ajax.markup.html.modal.theme.DefaultTheme;
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

        // modal
        final ModalDialog modal;
        add(modal = new ModalDialog("modalWindow"));
        modal.closeOnEscape().add(new DefaultTheme());

        Form<ActionFilter> filtersForm = new Form<ActionFilter>("filters-form");
        add(filtersForm);
        final AutoCompleteTextField<String> autoTextNameAction = new AutoCompleteTextField<String>("auto",
                                                                                                   new Model<>("")
        ) {

            @Override
            protected Iterator<String> getChoices(String input) {
                if (Strings.isEmpty(input)) {
                    return Collections.emptyIterator();
                }
                List<String>            choices = new ArrayList<>(20);
                List<UIActionForFilter> action  = actionService.getActionForFilter();
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

        UIAction   action = actionService.getAction(actionId);
        final long subId  = action.getSubsystemId();
        filtersForm.add(new Label("name-action", action.getActionName()));
        filtersForm.add(new Label("name-description", action.getActionDescription()));
        filtersForm.add(new Label("subname-action", action.getSubsystemName()));

        SortableDataProvider<UIActionForList, String> actionDataProvider = getUiActionForListStringSortableDataProvider(
                autoTextNameAction,
                subId
        );
        final DataView<UIActionForList> actionDataView = new PagingDataView<UIActionForList>("actionList",
                                                                                             actionDataProvider
        ) {

            @Override
            protected void populateItem(Item<UIActionForList> item) {
                final UIActionForList actionItem = item.getModelObject();
                AjaxLink<String> selectActionForCopy = new AjaxLink<>("select-action") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        long actionIdForCopy = actionItem.getId();
                        modal.setContent(new CopyActionWindow(modal, actionId, actionIdForCopy, getFeedbackPanel()));
                        modal.open(target);

                    }
                };
                selectActionForCopy.add(new Label("action-name", actionItem.getName()));
                item.add(selectActionForCopy);
                item.add(new Label("action-description", actionItem.getDescription()));
            }

        };
        filtersForm.add(actionDataView);
        filtersForm.add(new OrderByLink<>("order-by-actionName", "actionName", actionDataProvider));
        filtersForm.add(new OrderByLink<>("order-by-actionDescription", "actionDescription", actionDataProvider));
        filtersForm.add(new SuperflyPagingNavigator("paging-navigator", actionDataView));
        filtersForm.add(new BookmarkablePageLink<Page>("back", ListActionsPage.class));

    }

    private SortableDataProvider<UIActionForList, String> getUiActionForListStringSortableDataProvider(AutoCompleteTextField<String> autoTextNameAction, long subId) {
        String[] fieldName = {"actionId", "actionName", "actionDescription"};
        return new IndexedSortableDataProvider<>(fieldName) {
            public Iterator<? extends UIActionForList> iterator(long first, long count) {
                String     actionForFilter = autoTextNameAction.getModelObject();
                List<Long> subsystemId     = new ArrayList<Long>();
                subsystemId.add(subId);
                List<UIActionForList> actions = actionService.getActions(
                        first,
                        count,
                        getSortFieldIndex(),
                        isAscending(),
                        actionForFilter,
                        null,
                        subsystemId
                );
                return actions.iterator();
            }

            public long size() {
                List<Long> subsystemId = new ArrayList<Long>();
                subsystemId.add(subId);
                return actionService.getActionCount(null, null, subsystemId);

            }

        };
    }

    @Setter
    @Getter
    @SuppressWarnings("unused")
    private static class ActionFilter implements Serializable {
        private UIActionForFilter actionForFilter;
        private long              actionId;
    }

    @Override
    protected String getTitle() {
        return "Copy action";
    }

}

package com.payneteasy.superfly.web.wicket.page.action;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.action.UIActionForFilter;
import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.model.ui.action.UIActionWithGroupForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.ActionService;
import com.payneteasy.superfly.service.GroupService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.component.paging.SuperflyPagingNavigator;
import com.payneteasy.superfly.web.wicket.model.InitializingModel;
import com.payneteasy.superfly.web.wicket.model.StickyFilters;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.group.ViewGroupPage;
import com.payneteasy.superfly.web.wicket.repeater.IndexedSortableDataProvider;
import com.payneteasy.superfly.web.wicket.utils.ObjectHolder;
import com.payneteasy.superfly.web.wicket.utils.PageParametersBuilder;
import com.payneteasy.superfly.web.wicket.utils.WicketComponentHelper;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.Strings;
import org.springframework.security.access.annotation.Secured;

import java.io.Serializable;
import java.util.*;

@Secured("ROLE_ADMIN")
public class ListActionsPage extends BasePage {
    @SpringBean
    private ActionService actionService;
    @SpringBean
    private SubsystemService subsystemService;
    @SpringBean
    private GroupService groupService;

    public ListActionsPage() {
        super(ListActionsPage.class);

        final StickyFilters stickyFilters = getSession().getStickyFilters();
        Form<ActionFilter> filtersForm = new Form<ActionFilter>("filters-form");
        add(filtersForm);

        DropDownChoice<UISubsystemForFilter> subsystemDropdown = new DropDownChoice<UISubsystemForFilter>(
                "subsystem-filter"
                , new PropertyModel<>(stickyFilters, "subsystem")
                , subsystemService.getSubsystemsForFilter()
                , new SubsystemChoiceRenderer()
        );
        subsystemDropdown.setNullValid(true);
        filtersForm.add(subsystemDropdown);

        final AutoCompleteTextField<String> autoTextNameAction = new AutoCompleteTextField<String>("auto", new PropertyModel<String>(stickyFilters, "actionNameSubstring")) {
            @Override
            protected Iterator<String> getChoices(String input) {
                if (Strings.isEmpty(input)) {
                    return Collections.<String>emptyList().iterator();
                }
                List<String> choices = new ArrayList<String>(10);
                List<UIActionForFilter> action = actionService.getActionForFilter();
                for (UIActionForFilter uia : action) {
                    final String name = uia.getActionName();
                    if (name.toUpperCase().startsWith(input.toUpperCase())) {
                        choices.add(name);
                        if (choices.size() == 10) {
                            break;
                        }
                    }
                }
                return choices.iterator();
            }

        };
        filtersForm.add(autoTextNameAction);
        final List<Long> subsystemIds = new ArrayList<>();
        if (stickyFilters.getSubsystem() != null) {
            subsystemIds.add(stickyFilters.getSubsystem().getId());
        }
        final ObjectHolder<List<UIActionWithGroupForList>> actionsHolder = new ObjectHolder<>();
        final InitializingModel<Collection<UIActionWithGroupForList>> actionsCheckGroupModel = new InitializingModel<Collection<UIActionWithGroupForList>>() {

            @Override
            protected Collection<UIActionWithGroupForList> getInitialValue() {
                final Collection<UIActionWithGroupForList> checkedActions = new HashSet<>();
                for (UIActionWithGroupForList action : actionsHolder
                        .getObject()) {
                    if (action.isSelected()) {
                        checkedActions.add(action);
                    }
                }
                return checkedActions;
            }

        };

        String[] fieldName = {"actionId", "actionName", "actionDescription", "actionLog", "subsystemId", "subsystemName"};
        SortableDataProvider<UIActionWithGroupForList, String> actionDataProvider = new IndexedSortableDataProvider<UIActionWithGroupForList>(fieldName) {

            public Iterator<? extends UIActionWithGroupForList> iterator(long first,
                                                                long count) {
                UISubsystemForFilter subsystem = stickyFilters.getSubsystem();
                String actionForFilter = stickyFilters.getActionNameSubstring();
                if (subsystem == null) {
                    List<UIActionWithGroupForList> actions = actionService.getActionsWithGroup(first, count, getSortFieldIndex(), isAscending(), actionForFilter, null, null);
                    actionsHolder.setObject(actions);
                    actionsCheckGroupModel.clearInitialized();
                    return actions.iterator();
                } else {
                    List<Long> subsystemId = new ArrayList<Long>();
                    subsystemId.add(subsystem.getId());
                    List<UIActionWithGroupForList> actions = actionService.getActionsWithGroup(first, count, getSortFieldIndex(), isAscending(), actionForFilter, null, subsystemId);
                    actionsHolder.setObject(actions);
                    actionsCheckGroupModel.clearInitialized();
                    return actions.iterator();
                }
            }

            public long size() {
                UISubsystemForFilter subsystem = stickyFilters.getSubsystem();
                if (subsystem == null) {
                    return actionService.getActionCount(null, null, null);
                } else {
                    List<Long> subsystemId = new ArrayList<Long>();
                    subsystemId.add(subsystem.getId());
                    return actionService.getActionCount(null, null, subsystemId);
                }
            }

        };
        final Form<Void> form = new Form<Void>("form");
        add(form);
        final CheckGroup<UIActionWithGroupForList> checkGroup = new CheckGroup<UIActionWithGroupForList>("group", actionsCheckGroupModel) {
            @Override
            protected void onConfigure() {
                super.onConfigure();

                addOrReplace(new Label("group-name-header", Model.of("Group name")));

            }
        };
        form.add(checkGroup);
        checkGroup.add(new CheckGroupSelector("master-checkbox", checkGroup));

        final DataView<UIActionWithGroupForList> actionsDataView = new PagingDataView<UIActionWithGroupForList>("actionList", actionDataProvider) {

            @Override
            protected void populateItem(Item<UIActionWithGroupForList> item) {
                final UIActionForList action = item.getModelObject();
                item.add(new Label("action-name", action.getName()));
                item.add(new Label("action-description", action.getDescription()));
                item.add(new Label("subsystem-name", action.getSubsystemName()));
                item.add(new Check<>("selected", item.getModel(), checkGroup));
                item.add(new BookmarkablePageLink<Page>(
                                "copy-action",
                                CopyActionPropertiesPage.class,
                                PageParametersBuilder.fromPair("id", action.getId())
                        )
                );

                BookmarkablePageLink<Page> groupViewPage = new BookmarkablePageLink<>("group-page-link", ViewGroupPage.class, PageParametersBuilder.fromPair("gid", action.getGroupId()));
                groupViewPage.add(new Label("group-name", action.getGroupName()));
                item.add(groupViewPage);
                item.add(new Link<Void>("unmount-action-from-group") {
                    @Override
                    protected void onConfigure() {
                        super.onConfigure();
                        setVisible(action.getGroupId() != 0);
                    }

                    @Override
                    public void onClick() {
                        List<Long> actionToUnlink = new ArrayList<>();
                        actionToUnlink.add(action.getId());
                        RoutineResult routineResult = groupService.changeGroupActions(action.getGroupId(), null, actionToUnlink);
                        if (routineResult.isOk()) {
                            success("Action successfully unmount from group");
                        } else {
                            error(routineResult.getErrorMessage());
                        }
                    }
                });

                WicketComponentHelper.tableRowInfoCondition(item, action.isLogAction());

            }

        };
        checkGroup.add(actionsDataView);
        form.add(new Button("log-action") {

            @Override
            public void onSubmit() {
                List<Long> logOn = new ArrayList<Long>();
                List<Long> logOff = new ArrayList<Long>();
                Collection<UIActionWithGroupForList> checkedActions = actionsCheckGroupModel.getObject();
                for (UIActionWithGroupForList uia : actionsHolder.getObject()) {
                    if (checkedActions.contains(uia)) {
                        if (uia.isLogAction()) {
                            logOff.add(uia.getId());
                        }
                        logOn.add(uia.getId());
                    }
                }
                actionService.changeActionsLogLevel(logOn, logOff);
                setResponsePage(ListActionsPage.class);
            }

        });

        checkGroup.add(new OrderByLink<>("order-by-actionName", "actionName", actionDataProvider));
        checkGroup.add(new OrderByLink<>("order-by-actionDescription", "actionDescription", actionDataProvider));
        checkGroup.add(new OrderByLink<>("order-by-subsystemName", "subsystemName", actionDataProvider));

        checkGroup.add(new SuperflyPagingNavigator("paging-navigator", actionsDataView));

    }

    private Component invisibleComponent(String markupId) {
        return new WebMarkupContainer(markupId).setVisible(false);
    }

    @Override
    protected String getTitle() {
        return "Actions";
    }

    private class ActionFilter implements Serializable {
    }
}

package com.payneteasy.superfly.web.wicket.page.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.Strings;
import org.springframework.security.access.annotation.Secured;

import com.payneteasy.superfly.model.ui.action.UIActionForFilter;
import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.ActionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.model.InitializingModel;
import com.payneteasy.superfly.web.wicket.model.StickyFilters;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.repeater.IndexedSortableDataProvider;
import com.payneteasy.superfly.web.wicket.utils.ObjectHolder;

@Secured("ROLE_ADMIN")
public class ListActionsPage extends BasePage {
	@SpringBean
	private ActionService actionService;
	@SpringBean
	private SubsystemService subsystemService;

	public ListActionsPage() {
		super(ListActionsPage.class);
		
		final StickyFilters stickyFilters = getSession().getStickyFilters();
		Form<ActionFilter> filtersForm = new Form<ActionFilter>("filters-form");
		add(filtersForm);	
		
		DropDownChoice<UISubsystemForFilter> subsystemDropdown = new DropDownChoice<UISubsystemForFilter>(
				"subsystem-filter", new PropertyModel<UISubsystemForFilter>(
						stickyFilters, "subsystem"),
				subsystemService.getSubsystemsForFilter(),
				new SubsystemChoiceRenderer());
		subsystemDropdown.setNullValid(true);
		filtersForm.add(subsystemDropdown);
      
		final AutoCompleteTextField<String> autoTextNameAction = new AutoCompleteTextField<String>("auto",
				new PropertyModel<String>(stickyFilters, "actionNameSubstring")){
			@Override
			protected Iterator<String> getChoices(String input) {
				if (Strings.isEmpty(input))
                {
                    return Collections.<String>emptyList().iterator();
                }
				List<String> choices = new ArrayList<String>(10);
				List<UIActionForFilter> action = actionService.getActionForFilter();
				for(UIActionForFilter uia: action){
					final String name = uia.getActionName();
					if(name.toUpperCase().startsWith(input.toUpperCase())){
						choices.add(name);
						if(choices.size()==10){
							break;
						}
					}
				}
				return choices.iterator();
			}
			
		};
		filtersForm.add(autoTextNameAction);
		final List<Long> subsystemIds = new ArrayList<Long>();
		if (stickyFilters.getSubsystem() != null) {
			subsystemIds.add(stickyFilters.getSubsystem().getId());
		}
		final ObjectHolder<List<UIActionForList>> actionsHolder = new ObjectHolder<List<UIActionForList>>();
		final InitializingModel<Collection<UIActionForList>> actionsCheckGroupModel = new InitializingModel<Collection<UIActionForList>>() {

			@Override
			protected Collection<UIActionForList> getInitialValue() {
				final Collection<UIActionForList> checkedActions = new HashSet<UIActionForList>();
				for (UIActionForList action : actionsHolder
						.getObject()) {
					if (action.isSelected()) {
						checkedActions.add(action);
					}
				}
				return checkedActions;
			}

		};
		
		String[] fieldName = { "actionId","actionName", "actionDescription","actionLog","subsystemId" ,"subsystemName"};
		SortableDataProvider<UIActionForList> actionDataProvider = new IndexedSortableDataProvider<UIActionForList>(
				fieldName) {

			public Iterator<? extends UIActionForList> iterator(int first,
					int count) {
				UISubsystemForFilter subsystem = stickyFilters.getSubsystem();
				String actionForFilter = stickyFilters.getActionNameSubstring();
				List<Long> subsystemId = new ArrayList<Long>();
				if (subsystem == null) {
					List<UIActionForList> actions = actionService.getActions(first, count,
							getSortFieldIndex(), isAscending(), actionForFilter == null ? null : actionForFilter, null,
							subsystem == null ? null : null);
					actionsHolder.setObject(actions);
					actionsCheckGroupModel.clearInitialized();
					return actions.iterator();
				} else {
					subsystemId.add(subsystem.getId());
					List<UIActionForList> actions = actionService.getActions(first, count,
							getSortFieldIndex(), isAscending(), actionForFilter == null ? null : actionForFilter, null,
							subsystem == null ? null : subsystemId);
					actionsHolder.setObject(actions);
					actionsCheckGroupModel.clearInitialized();
					return actions.iterator();
				}
			}

			public int size() {
				UISubsystemForFilter subsystem = stickyFilters.getSubsystem();
				List<Long> subsystemId = new ArrayList<Long>();
				if(subsystem == null){
					return actionService.getActionCount(null, null, subsystem == null ? null : null);
				}else{
					subsystemId.add(subsystem.getId());
					return actionService.getActionCount(null, null, subsystem == null ? null : subsystemId);
				}
			}

		};
		final Form<Void> form = new Form<Void>("form") {
		};
		add(form);
		final CheckGroup<UIActionForList> group = new CheckGroup<UIActionForList>(
				"group", actionsCheckGroupModel);
		form.add(group);
		group.add(new CheckGroupSelector("master-checkbox", group));
		
		final DataView<UIActionForList> actionsDataView = new PagingDataView<UIActionForList>("actionList",actionDataProvider){

			@Override
			protected void populateItem(Item<UIActionForList> item) {
				final UIActionForList action = item.getModelObject();
				item.add(new Label("action-name",action.getName()));
				item.add(new Label("action-description",action.getDescroption()));
				item.add(new Label("subsystem-name",action.getSubsystemName()));
				Link<Void> switchLogLevel = new Link<Void>("switch-loglevel") {

					@Override
					public void onClick() {
						List<Long> logLevelOn = new ArrayList<Long>();
						List<Long> logLevelOff = new ArrayList<Long>();
						if (action.isLogAction()) {
							logLevelOff.add(action.getId());
							actionService.changeActionsLogLevel(null, logLevelOff);
						} else {
							logLevelOn.add(action.getId());
							actionService.changeActionsLogLevel(logLevelOn, null);
						}
						
					}

				};
				switchLogLevel.add(new Label("log-action", action
						.isLogAction() ? "yes" : "NO"));
				item.add(switchLogLevel);
				item.add(new Check<UIActionForList>("selected", item.getModel(),group));
				item.add(new BookmarkablePageLink<Page>("copy-action",
						CopyActionPropertiesPage.class ).setParameter("id", action.getId()));
				
			}
			
		};
		group.add(actionsDataView);
		form.add(new Button("log-action"){

			@Override
			public void onSubmit() {
				List<Long> logOn = new ArrayList<Long>();
				List<Long> logOff = new ArrayList<Long>();
				Collection<UIActionForList> checkedActions=actionsCheckGroupModel.getObject();
				for(UIActionForList uia: actionsHolder.getObject()){
					if(checkedActions.contains(uia)){
						if(uia.isLogAction()){
							logOff.add(uia.getId());
						}
						logOn.add(uia.getId());
					}
				}
				actionService.changeActionsLogLevel(logOn, logOff);
				setResponsePage(ListActionsPage.class);
			}
	    	
	    });
                
		group.add(new OrderByLink("order-by-actionName", "actionName", actionDataProvider));
		group.add(new OrderByLink("order-by-actionDescription", "actionDescription",
				actionDataProvider));
		group.add(new OrderByLink("order-by-subsystemName", "subsystemName", actionDataProvider));
		group.add(new OrderByLink("order-by-actionLog","actionLog",actionDataProvider));
		form.add(new PagingNavigator("paging-navigator", actionsDataView));

	}

	@Override
	protected String getTitle() {
		return "Actions";
	}
	
	private class ActionFilter implements Serializable {
	}
}

package com.payneteasy.superfly.web.wicket.page.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.Strings;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.action.UIActionForFilter;
import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.ActionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.repeater.IndexedSortableDataProvider;

@Secured("ROLE_ADMIN")
public class ListActionsPage extends BasePage {
	@SpringBean
	private ActionService actionService;
	@SpringBean
	private SubsystemService subsystemService;

	public ListActionsPage() {
		super();
		final ActionFilter actionFilter = new ActionFilter();
		Form<ActionFilter> filtersForm = new Form<ActionFilter>("filters-form");
		add(filtersForm);
		
		//Filter for subsystems
		DropDownChoice<UISubsystemForFilter> subsystemDropdown = new DropDownChoice<UISubsystemForFilter>(
				"subsystem-filter", new PropertyModel<UISubsystemForFilter>(
						actionFilter, "subsystem"), subsystemService
						.getSubsystemsForFilter(),
				new SubsystemChoiceRenderer());
		subsystemDropdown.setNullValid(true);
		filtersForm.add(subsystemDropdown);
       //Filter for actionName
		final AutoCompleteTextField<String> autoTextNameAction = new AutoCompleteTextField<String>("auto",new Model("")){

			@Override
			protected Iterator getChoices(String input) {
				if (Strings.isEmpty(input))
                {
                    return Collections.EMPTY_LIST.iterator();
                }
				List choices = new ArrayList(10);
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
		/*autoTextNameAction.add(new AjaxFormSubmitBehavior(filtersForm,"onchange"){

			@Override
			protected void onError(AjaxRequestTarget target) {
				
				
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				
				
			}
			
		});*/
		
		final List<Long> subsystemIds = new ArrayList<Long>();
		if (actionFilter.getSubsystem() != null) {

			subsystemIds.add(actionFilter.getSubsystem().getId());
		}

		String[] fieldName = { "actionId", "actionName", "actionDescription",
				"subsystemName" };
		SortableDataProvider<UIActionForList> actionDataProvider = new IndexedSortableDataProvider<UIActionForList>(
				fieldName) {

			public Iterator<? extends UIActionForList> iterator(int first,
					int count) {
				UISubsystemForFilter subsystem = actionFilter.getSubsystem();
				String actionForFilter = autoTextNameAction.getModelObject();
				List<Long> subsystemId = new ArrayList<Long>();
				if (subsystem == null) {
					List<UIActionForList> actions = actionService.getActions(first, count,
							getSortFieldIndex(), isAscending(), actionForFilter==null?null:actionForFilter, null,
							subsystem == null ? null : null);
					setDataset(actions);
					return actions.iterator();
				} else {
					subsystemId.add(subsystem.getId());
					List<UIActionForList> actions = actionService.getActions(first, count,
							getSortFieldIndex(), isAscending(), actionForFilter==null?null:actionForFilter, null,
							subsystem == null ? null : subsystemId);
					setDataset(actions);
					return actions.iterator();
				}
			}

			public int size() {
				UISubsystemForFilter subsystem = actionFilter.getSubsystem();
				List<Long> subsystemId = new ArrayList<Long>();
				if(subsystem==null){
					return actionService.getActionCount(null, null, subsystem == null ? null : null);
				}else{
					subsystemId.add(subsystem.getId());
					return actionService.getActionCount(null, null, subsystem == null ? null : subsystemId);
				}
			}

		};
		final DataView<UIActionForList> actionDataView = new PagingDataView<UIActionForList>("actionList",actionDataProvider){

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
				item.add(new CheckBox("selected", new PropertyModel<Boolean>(action, "selected")));
				item.add(new BookmarkablePageLink("copy-action",
						CopyActionPropertiesPage.class ).setParameter("id", action.getId()));
			}
			
		};
		filtersForm.add(actionDataView);
	    filtersForm.add(new Button("log-action"){

			@Override
			public void onSubmit() {
				List<Long> logOn = new ArrayList<Long>();
				List<Long> logOff = new ArrayList<Long>();
				for(UIActionForList uia: ((IndexedSortableDataProvider<UIActionForList>)actionDataView.getDataProvider()).getDataset()){
					if(uia.isSelected()){
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
                
	    filtersForm.add(new OrderByLink("order-by-actionName", "actionName", actionDataProvider));
	    filtersForm.add(new OrderByLink("order-by-actionDescription", "actionDescription",
				actionDataProvider));
	    filtersForm.add(new OrderByLink("order-by-subsystemName", "subsystemName", actionDataProvider));
	    filtersForm.add(new PagingNavigator("paging-navigator", actionDataView));

	}

	@SuppressWarnings("unused")
	private class ActionFilter implements Serializable {
		private UISubsystemForFilter subsystem;
		private UIActionForFilter actionForFilter;

		public UIActionForFilter getActionForFilter() {
			return actionForFilter;
		}

		public void setActionForFilter(UIActionForFilter actionForFilter) {
			this.actionForFilter = actionForFilter;
		}

		public UISubsystemForFilter getSubsystem() {
			return subsystem;
		}

		public void setSubsystem(UISubsystemForFilter subsystem) {
			this.subsystem = subsystem;
		}

	}
	
	@Override
	protected String getTitle() {
		return "Actions";
	}
}

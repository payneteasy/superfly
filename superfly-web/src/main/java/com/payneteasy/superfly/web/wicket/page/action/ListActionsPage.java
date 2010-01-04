package com.payneteasy.superfly.web.wicket.page.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.ActionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.group.SubsystemModel;

public class ListActionsPage extends BasePage {
	@SpringBean
	private ActionService actionService;
	@SpringBean
	private SubsystemService subsystemService;
    
	@SuppressWarnings( { "serial", "unchecked" })
	public ListActionsPage() {
		 
		final SubsystemModel subsystemModel = new SubsystemModel();

		//setDefaultModel(new CompoundPropertyModel<SubsystemModel>(
		//		subsystemModel));

		Form form = new Form("form") {

			

		};
		form.add(new DropDownChoice<UISubsystemForFilter>(
				"subsystem-filter", new PropertyModel<UISubsystemForFilter>(subsystemModel,"uiSubsystemForFilter"),subsystemService
						.getSubsystemsForFilter(),
				new SubsystemChoiceRenderer()).setNullValid(true));
		add(form);
		IModel actionListModel = new LoadableDetachableModel() {

			@Override
			protected Object load() {
				List<Long> subsystemIds = new ArrayList<Long>();
				if(subsystemModel.getUiSubsystemForFilter() != null){
					
					subsystemIds.add(subsystemModel.getUiSubsystemForFilter().getId());
				}
				List<UIActionForList> actionList = actionService.getAction(subsystemIds);
				return actionList;
			}
			
		};
		ListView<UIActionForList> listViewAction = new ListView<UIActionForList>(
				"list-action", actionListModel) {

			@Override
			protected void populateItem(ListItem<UIActionForList> item) {
				UIActionForList action = item.getModelObject();
				item.add(new Label("name-action", action.getName()));
				item.add(new Label("description-action", action
						.getDescroption()));
				item
						.add(new Label("subsystem-name", action
								.getSubsystemName()));
				item.add(new Label("log-action", action.isLogAction() ? "yes"
						: "NO"));
			}
		};
		add(listViewAction);
	}
}

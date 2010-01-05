package com.payneteasy.superfly.web.wicket.page.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.ActionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.SelectObjectWrapper;
import com.payneteasy.superfly.web.wicket.page.group.SubsystemModel;

public class ListActionsPage extends BasePage {
	@SpringBean
	private ActionService actionService;
	@SpringBean
	private SubsystemService subsystemService;

	@SuppressWarnings( { "serial", "unchecked" })
	public ListActionsPage() {

		final SubsystemModel subsystemModel = new SubsystemModel();
		Form form = new Form("form"){
		};
		add(form);
		form.add(new DropDownChoice<UISubsystemForFilter>("subsystem-filter",
				new PropertyModel<UISubsystemForFilter>(subsystemModel,
						"uiSubsystemForFilter"), subsystemService
						.getSubsystemsForFilter(),
				new SubsystemChoiceRenderer()).setNullValid(true));
		IModel actionListModel = new LoadableDetachableModel() {

			@Override
			protected Object load() {
				final List<Long> subsystemIds = new ArrayList<Long>();
				if (subsystemModel.getUiSubsystemForFilter() != null) {

					subsystemIds.add(subsystemModel.getUiSubsystemForFilter()
							.getId());
				}
				List<UIActionForList> actionList = actionService
						.getAction(subsystemIds);
				List<SelectObjectWrapper<UIActionForList>> actionListWrapper = new ArrayList<SelectObjectWrapper<UIActionForList>>();
				for (UIActionForList uia : actionList) {
					actionListWrapper
							.add(new SelectObjectWrapper<UIActionForList>(uia));
				}

				return actionListWrapper;
			}

		};

		final ListView<SelectObjectWrapper<UIActionForList>> listViewAction = new ListView<SelectObjectWrapper<UIActionForList>>(
				"list-action", actionListModel) {

			@Override
			protected void populateItem(
					ListItem<SelectObjectWrapper<UIActionForList>> item) {
				final SelectObjectWrapper<UIActionForList> action = item
						.getModelObject();
				item
						.add(new Label("name-action", action.getObject()
								.getName()));
				item.add(new Label("description-action", action.getObject()
						.getDescroption()));
				item.add(new Label("subsystem-name", action.getObject()
						.getSubsystemName()));
				Link<Void> switchLogLevel = new Link<Void>("switch-loglevel") {

					@Override
					public void onClick() {
						List<Long> logLevelOn = new ArrayList<Long>();
						List<Long> logLevelOff = new ArrayList<Long>();
						if (action.getObject().isLogAction()) {
							logLevelOff.add(action.getObject().getId());
							actionService.changeActionsLogLevel(null, logLevelOff);
						} else {
							logLevelOn.add(action.getObject().getId());
							actionService.changeActionsLogLevel(logLevelOn, null);
						}
						
					}

				};
				switchLogLevel.add(new Label("log-action", action.getObject()
						.isLogAction() ? "yes" : "NO"));
				item.add(switchLogLevel);
				item.add(new CheckBox("selected", new PropertyModel<Boolean>(
						action, "selected")));
			}
		};
		Form formList = new Form("formList") {
			@Override
			protected void onSubmit() {
				ArrayList<SelectObjectWrapper<UIActionForList>> actionListWrapper = (ArrayList<SelectObjectWrapper<UIActionForList>>) listViewAction
						.getModelObject();
				List<Long> listLogLevelOn = new ArrayList<Long>();
				List<Long> listLogLevelOff = new ArrayList<Long>();
				for (SelectObjectWrapper<UIActionForList> suia : actionListWrapper) {
					if (suia.isSelected()) {
						if (suia.getObject().isLogAction()) {
							listLogLevelOff.add(suia.getObject().getId());
						}
						listLogLevelOn.add(suia.getObject().getId());
					}
				}
				actionService.changeActionsLogLevel(listLogLevelOn,
						listLogLevelOff);
				setResponsePage(ListActionsPage.class);
			}

		};
		formList.add(listViewAction);
		formList.add(new CheckBox("groupselector", new Model("empty")) {
			@Override
			public void onSelectionChanged(Object newSelection) {
				boolean flag = ((Boolean) newSelection).booleanValue();
				for (SelectObjectWrapper<UIActionForList> suia : listViewAction
						.getModelObject()) {
					suia.setSelected(flag);
				}
			}

			public boolean wantOnSelectionChangedNotifications() {
				return true;
			}
		});
		add(formList);

	}
}

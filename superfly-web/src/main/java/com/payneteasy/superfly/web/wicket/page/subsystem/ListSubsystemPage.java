package com.payneteasy.superfly.web.wicket.page.subsystem;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.ConfirmPanel;
import com.payneteasy.superfly.web.wicket.page.EmptyPanel;
import com.payneteasy.superfly.web.wicket.page.SelectObjectWrapper;

public class ListSubsystemPage extends BasePage {
	@SpringBean
	private SubsystemService subsystemService;

	@SuppressWarnings("unchecked")
	public ListSubsystemPage() {
		add(new EmptyPanel("confirmPanel"));

		List<UISubsystemForList> subsystems = subsystemService.getSubsystems();
		List<SelectObjectWrapper<UISubsystemForList>> subsystemWrapper = new ArrayList<SelectObjectWrapper<UISubsystemForList>>();
		for (UISubsystemForList ui : subsystems) {
			subsystemWrapper
					.add(new SelectObjectWrapper<UISubsystemForList>(ui));
		}
		final ListView<SelectObjectWrapper<UISubsystemForList>> listView = new ListView<SelectObjectWrapper<UISubsystemForList>>(
				"list-subsystem", subsystemWrapper) {

			@Override
			protected void populateItem(
					ListItem<SelectObjectWrapper<UISubsystemForList>> item) {
				SelectObjectWrapper<UISubsystemForList> subWrapperItem = item
						.getModelObject();
				item.add(new Label("subsytem-name", subWrapperItem.getObject()
						.getName()));
				item.add(new Label("subsystem-identifier", subWrapperItem
						.getObject().getIdentifier()));
				item.add(new BookmarkablePageLink("subsystem-edit",
						EditSubsytemPage.class).setParameter("id", subWrapperItem.getObject().getId()));
				item.add(new CheckBox("selected", new PropertyModel<Boolean>(
						subWrapperItem, "selected")));
				item.add(new Label("subsystem-callback",subWrapperItem.getObject().getCallbackInformation()));
			}

		};
		Form form = new Form("form") {

			@Override
			protected void onSubmit() {
				ArrayList<SelectObjectWrapper<UISubsystemForList>> subsystemsWrap = (ArrayList<SelectObjectWrapper<UISubsystemForList>>) listView
						.getModelObject();
				final ArrayList<SelectObjectWrapper<UISubsystemForList>> listWrap = new ArrayList<SelectObjectWrapper<UISubsystemForList>>();
				for (SelectObjectWrapper<UISubsystemForList> ui : subsystemsWrap) {
					if (ui.isSelected()) {
						listWrap.add(ui);
					}
				}
				if (listWrap.size() == 0)
					return;
				this.getParent().get("confirmPanel").replaceWith(
						new ConfirmPanel("confirmPanel",
								"You are about to delete "
										+ " subsystem(s) permanently?") {
							public void onConfirm() {
								for (SelectObjectWrapper<UISubsystemForList> ui : listWrap)
									subsystemService.deleteSubsystem(ui
											.getObject().getId());
								this.getParent().setResponsePage(
										ListSubsystemPage.class);
							}

							public void onCancel() {
								this.getPage().get("confirmPanel").replaceWith(
										new EmptyPanel("confirmPanel"));
							}
						});
			}

		};
		form.add(listView);
		form.add(new CheckBox("groupselector", new Model("empty")) {
			@Override
			public void onSelectionChanged(Object newSelection) {
				boolean flag = ((Boolean) newSelection).booleanValue();
				for (SelectObjectWrapper<UISubsystemForList> ssw : listView
						.getModelObject()) {
					ssw.setSelected(flag);
				}
			}

			public boolean wantOnSelectionChangedNotifications() {
				return true;
			}
		});
		form.add(new Button("add-subsystem"){

			@Override
			public void onSubmit() {
				setResponsePage(AddSubsystemPage.class);
			}
			
		}.setDefaultFormProcessing(false));
		add(form);
	}
}

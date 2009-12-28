package com.payneteasy.superfly.web.wicket.page.group;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.payneteasy.superfly.model.ui.group.UIGroupForList;
import com.payneteasy.superfly.service.GroupService;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.ConfirmPanel;
import com.payneteasy.superfly.web.wicket.page.EmptyPanel;
import com.payneteasy.superfly.web.wicket.page.SelectObjectWrapper;

public class ListGroupPage extends BasePage {
	@SpringBean
	private GroupService groupService;

	@SuppressWarnings("unchecked")
	public ListGroupPage() {
		add(new EmptyPanel("confirmPanel"));

		List<UIGroupForList> groups = groupService.getGroups();
		List<SelectObjectWrapper<UIGroupForList>> groupsWrapper = new ArrayList<SelectObjectWrapper<UIGroupForList>>();
		for (UIGroupForList ui : groups) {
			groupsWrapper.add(new SelectObjectWrapper<UIGroupForList>(ui));
		}

		final ListView<SelectObjectWrapper<UIGroupForList>> listViewGroups = new ListView<SelectObjectWrapper<UIGroupForList>>(
				"list-group", groupsWrapper) {

			@Override
			protected void populateItem(
					ListItem<SelectObjectWrapper<UIGroupForList>> item) {
				SelectObjectWrapper<UIGroupForList> groups = item
						.getModelObject();
				item.add(new Label("name-group", groups.getObject().getName()));
				item.add(new CheckBox("selected", new PropertyModel<Boolean>(
						groups, "selected")));

			}

		};
		Form form = new Form("form") {

			@Override
			protected void onSubmit() {
				ArrayList<SelectObjectWrapper<UIGroupForList>> listgroupWrapper = (ArrayList<SelectObjectWrapper<UIGroupForList>>) listViewGroups
						.getModelObject();
				final ArrayList<SelectObjectWrapper<UIGroupForList>> uiWrap = new ArrayList<SelectObjectWrapper<UIGroupForList>>();
				for (SelectObjectWrapper<UIGroupForList> uiw : listgroupWrapper) {
					if (uiw.isSelected()) {
						uiWrap.add(uiw);
					}
				}
				if (uiWrap.size() == 0)
					return;
				this.getParent().get("confirmPanel").replaceWith(
						new ConfirmPanel("confirmPanel",
								"You are about to delete "
										+ " group(s) permanently?") {
							public void onConfirm() {
								for (SelectObjectWrapper<UIGroupForList> ui : uiWrap)
									groupService.deleteGorup(ui.getObject()
											.getId());
								this.getParent().setResponsePage(
										ListGroupPage.class);
							}

							public void onCancel() {
								this.getPage().get("confirmPanel").replaceWith(
										new EmptyPanel("confirmPanel"));
							}
						});
			}

		};
		form.add(listViewGroups);
		form.add(new CheckBox("groupselector", new Model("empty")) {
			@Override
			public void onSelectionChanged(Object newSelection) {
				boolean flag = ((Boolean) newSelection).booleanValue();
				for (SelectObjectWrapper<UIGroupForList> ssw : listViewGroups
						.getModelObject()) {
					ssw.setSelected(flag);
				}
			}

			public boolean wantOnSelectionChangedNotifications() {
				return true;
			}
		});
		form.add(new Button("add-group"){

			@Override
			public void onSubmit() {
				setResponsePage(AddGroupPage.class);
			}
			
		}.setDefaultFormProcessing(false));
		add(form);
	}
}

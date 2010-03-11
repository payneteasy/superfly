package com.payneteasy.superfly.web.wicket.page.subsystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.ConfirmPanel;
import com.payneteasy.superfly.web.wicket.model.InitializingModel;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.SelectObjectWrapper;

@Secured("ROLE_ADMIN")
public class ListSubsystemsPage extends BasePage {
	@SpringBean
	private SubsystemService subsystemService;

	@SuppressWarnings("unchecked")
	public ListSubsystemsPage() {
		add(new EmptyPanel("confirmPanel"));

		List<UISubsystemForList> subsystems = subsystemService.getSubsystems();
		final List<SelectObjectWrapper<UISubsystemForList>> subsystemWrapper = new ArrayList<SelectObjectWrapper<UISubsystemForList>>();
		for (UISubsystemForList ui : subsystems) {
			subsystemWrapper
					.add(new SelectObjectWrapper<UISubsystemForList>(ui));
		}
		final InitializingModel<Collection<SelectObjectWrapper<UISubsystemForList>>> subsystemsCheckGroupModel = new InitializingModel<Collection<SelectObjectWrapper<UISubsystemForList>>>() {

			@Override
			protected Collection<SelectObjectWrapper<UISubsystemForList>> getInitialValue() {
				final Collection<SelectObjectWrapper<UISubsystemForList>> checkedSubsystems = new HashSet<SelectObjectWrapper<UISubsystemForList>>();
				for (SelectObjectWrapper<UISubsystemForList> subsystem : subsystemWrapper) {
					if (subsystem.isSelected()) {
						checkedSubsystems.add(subsystem);
					}
				}
				return checkedSubsystems;
			}

		};
		Form<Void> form = new Form<Void>("form") {

		};
		final CheckGroup<SelectObjectWrapper<UISubsystemForList>> group = new CheckGroup<SelectObjectWrapper<UISubsystemForList>>(
				"group", subsystemsCheckGroupModel);
		form.add(group);
		final ListView<SelectObjectWrapper<UISubsystemForList>> listView = new ListView<SelectObjectWrapper<UISubsystemForList>>(
				"list-subsystem", subsystemWrapper) {

			@Override
			protected void populateItem(
					ListItem<SelectObjectWrapper<UISubsystemForList>> item) {
				final SelectObjectWrapper<UISubsystemForList> subWrapperItem = item
						.getModelObject();
				item.add(new Label("subsytem-name", subWrapperItem.getObject()
						.getName()));
				item.add(new BookmarkablePageLink("subsystem-edit",
						EditSubsystemPage.class).setParameter("id",
						subWrapperItem.getObject().getId()));
				item.add(new Check<SelectObjectWrapper<UISubsystemForList>>(
						"selected", item.getModel()));
				item.add(new Label("subsystem-callback", subWrapperItem
						.getObject().getCallbackInformation()));
				item.add(new Label("allow-list-users",
						item.getModelObject().getObject().isAllowListUsers() ? "Yes" : "No"));
				item.add(new SubmitLink("delete-subsystem") {

					@Override
					public void onSubmit() {
						this.getPage().get("confirmPanel").replaceWith(
								new ConfirmPanel("confirmPanel",
										"You are about to delete "
												+ " subsystem - "
												+ subWrapperItem.getObject()
														.getName()
												+ " permanently?") {
									public void onConfirm() {

										subsystemService
												.deleteSubsystem(subWrapperItem
														.getObject().getId());

										this.getPage().setResponsePage(
												ListSubsystemsPage.class);
									}

									public void onCancel() {
										this
												.getPage()
												.get("confirmPanel")
												.replaceWith(
														new EmptyPanel(
																"confirmPanel"));
									}
								});
					}

				});
			}

		};
		group.add(listView);
		group.add(new CheckGroupSelector("master-checkbox", group));
		form.add(new Button("delete-sub") {

			@Override
			public void onSubmit() {
				ArrayList<SelectObjectWrapper<UISubsystemForList>> subsystemsWrap = (ArrayList<SelectObjectWrapper<UISubsystemForList>>) listView
						.getModelObject();
				Collection<SelectObjectWrapper<UISubsystemForList>> checkedSubsystems = subsystemsCheckGroupModel
						.getObject();
				final ArrayList<SelectObjectWrapper<UISubsystemForList>> listWrap = new ArrayList<SelectObjectWrapper<UISubsystemForList>>();
				for (SelectObjectWrapper<UISubsystemForList> ui : subsystemsWrap) {
					if (checkedSubsystems.contains(ui)) {
						listWrap.add(ui);
					}
				}
				if (listWrap.size() == 0)
					return;
				this.getPage().get("confirmPanel").replaceWith(
						new ConfirmPanel("confirmPanel",
								"You are about to delete "
										+ " subsystem(s) permanently?") {
							public void onConfirm() {
								boolean someSuccess = false;
								for (SelectObjectWrapper<UISubsystemForList> ui : listWrap) {
									RoutineResult result = subsystemService.deleteSubsystem(
											ui.getObject().getId());
									if (result.isOk()) {
										someSuccess = true;
									}
								}
								if (someSuccess) {
									info("Deleted subsystems; please be aware that some sessions could be expired");
								}
								this.getPage().setResponsePage(
										ListSubsystemsPage.class);
							}

							public void onCancel() {
								this.getPage().get("confirmPanel").replaceWith(
										new EmptyPanel("confirmPanel"));
							}
						});
			}

		});
		form.add(new Button("add-subsystem") {

			@Override
			public void onSubmit() {
				setResponsePage(AddSubsystemPage.class);
			}

		}.setDefaultFormProcessing(false));
		add(form);
	}

	@Override
	protected String getTitle() {
		return "Subsystems";
	}
}

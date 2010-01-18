package com.payneteasy.superfly.web.wicket.page.group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.group.UIGroupForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.GroupService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.ConfirmPanel;
import com.payneteasy.superfly.web.wicket.component.EmptyPanel;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.group.wizard.GroupActionsPage;
import com.payneteasy.superfly.web.wicket.page.group.wizard.GroupPropertiesPage;
import com.payneteasy.superfly.web.wicket.repeater.IndexedSortableDataProvider;

@Secured("ROLE_ADMIN")
public class ListGroupsPage extends BasePage {
	@SpringBean
	private GroupService groupService;
	@SpringBean
	private SubsystemService ssysService;
	
	@SuppressWarnings({ "unchecked", "serial" })
	public ListGroupsPage() {
		// CONFIRM PANEL
		add(new EmptyPanel("confirmPanel"));
		
		// FILTER
		final GroupFilter groupFilter = new GroupFilter();
		Form<GroupFilter> filtersForm = new Form<GroupFilter>("filters-form");
		add(filtersForm);
		DropDownChoice<UISubsystemForFilter> subsystemDropdown = new DropDownChoice<UISubsystemForFilter>(
				"subsystem-filter", new PropertyModel<UISubsystemForFilter>(
						groupFilter, "subsystem"), ssysService
						.getSubsystemsForFilter(),
				new SubsystemChoiceRenderer());
		subsystemDropdown.setNullValid(true);
		filtersForm.add(subsystemDropdown);
		final List<Long> subsystemIds = new ArrayList<Long>();
		if (groupFilter.getSubsystem() != null) subsystemIds.add(groupFilter.getSubsystem().getId());

		// SORTABLE DATA PROVIDER
		String[] fieldName = { "groupName", "groupSubsystem"};
		final SortableDataProvider<UIGroupForList> groupDataProvider = new IndexedSortableDataProvider<UIGroupForList>(
				fieldName) {
						
			public Iterator<? extends UIGroupForList> iterator(int first,
					int count) {
				UISubsystemForFilter subsystem = groupFilter.getSubsystem();
				List<Long> subsystemId = new ArrayList<Long>();
				if (subsystem == null) {
					List<UIGroupForList> list = groupService.getGroupsForSubsystems(first, count,
							getSortFieldIndex(), isAscending(), null,
							subsystem == null ? null : null);
					return list.iterator();
				} else {
					subsystemId.add(subsystem.getId());
					List<UIGroupForList> list = groupService.getGroupsForSubsystems(first, count,
							getSortFieldIndex(), isAscending(), null,
							subsystem == null ? null : subsystemId);
					return list.iterator(); 
				}
			}

			public int size() {
				UISubsystemForFilter subsystem = groupFilter.getSubsystem();
				List<Long> subsystemId = new ArrayList<Long>();
				if(subsystem==null){
					return groupService.getGroupsCount(null, subsystem == null ? null : null);
				}else{
					subsystemId.add(subsystem.getId());
					return groupService.getGroupsCount(null, subsystem == null ? null : subsystemId);
				}
			}

		};

		
		
//		DATAVIEW
		final CheckGroup<UIGroupForList> checkGroup = new CheckGroup<UIGroupForList>("group", new ArrayList());
		
		final DataView<UIGroupForList> groupDataView = new PagingDataView<UIGroupForList>("list-group",groupDataProvider){
			@Override
			protected void populateItem(Item<UIGroupForList> item) {
				final UIGroupForList group = item.getModelObject();
				item.add(new Check<UIGroupForList>("selected", item.getModel(), checkGroup));
				item.add(new Label("name-group",group.getName()));
				item.add(new Label("ssys-group",group.getSubsystemName()));
				item.add(new BookmarkablePageLink("group-view",
						ViewGroupPage.class).setParameter("gid",group.getId()));
				item.add(new BookmarkablePageLink("group-edit",
						GroupPropertiesPage.class).setParameter("gid",group.getId()));
				item.add(new BookmarkablePageLink("group-actions",
						GroupActionsPage.class).setParameter("gid",group.getId()));
			}
			
		};
	
		Form form = new Form("form") {

			@Override
			protected void onSubmit() {
				final ArrayList<UIGroupForList> list = (ArrayList<UIGroupForList>) checkGroup.getModelObject();
				
				if (list.size() == 0)	return;
				
				this.getParent().get("confirmPanel").replaceWith(
					new ConfirmPanel("confirmPanel",
							"You are about to delete "+ list.size()
									+ " group(s) permanently?") {
						public void onConfirm() {
							for (UIGroupForList ui : list)
								groupService.deleteGroup(ui.getId());
							this.getParent().setResponsePage(
									ListGroupsPage.class);
						}

						public void onCancel() {
							this.getPage().get("confirmPanel").replaceWith(
									new EmptyPanel("confirmPanel"));
						}
					});
				
			}

		};
		
		checkGroup.add(groupDataView);
		checkGroup.add(new OrderByLink("order-by-groupName", "groupName", groupDataProvider));
		checkGroup.add(new OrderByLink("order-by-subsystemName", "groupSubsystem", groupDataProvider));
		checkGroup.add(new PagingNavigator("paging-navigator", groupDataView));
		
		checkGroup.add(new CheckGroupSelector("groupselector"));
		checkGroup.add(new Button("add-group"){
			@Override
			public void onSubmit() {
				setResponsePage(GroupPropertiesPage.class);
			}
			
		}.setDefaultFormProcessing(false));
		
		form.add(checkGroup);
		add(form);
	}

@SuppressWarnings("unused")
private class GroupFilter implements Serializable {
	private static final long serialVersionUID = 1L;
	private UISubsystemForFilter subsystem;

	public UISubsystemForFilter getSubsystem() {
		return subsystem;
	}

	public void setSubsystem(UISubsystemForFilter subsystem) {
		this.subsystem = subsystem;
	}

}

@Override
protected String getTitle() {
	return "Groups";
}


}
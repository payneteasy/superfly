package com.payneteasy.superfly.web.wicket.page.group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.payneteasy.superfly.model.ui.group.UIGroupForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.GroupService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.ConfirmPanel;
import com.payneteasy.superfly.web.wicket.page.EmptyPanel;
import com.payneteasy.superfly.web.wicket.repeater.IndexedSortableDataProvider;

public class ListGroupsPage extends BasePage {
	@SpringBean
	private GroupService groupService;
	@SpringBean
	private SubsystemService ssysService;
	
	@SuppressWarnings({ "unchecked", "serial" })
	public ListGroupsPage() {
		// CONFIRM PANEL
		add(new EmptyPanel("confirmPanel"));

		// FILTER PANEL
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
					setDataset(list);
					return list.iterator();
				} else {
					subsystemId.add(subsystem.getId());
					List<UIGroupForList> list = groupService.getGroupsForSubsystems(first, count,
							getSortFieldIndex(), isAscending(), null,
							subsystem == null ? null : subsystemId);
					setDataset(list);
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
		
		final DataView<UIGroupForList> groupDataView = new PagingDataView<UIGroupForList>("list-group",groupDataProvider){
			@Override
			protected void populateItem(Item<UIGroupForList> item) {
				final UIGroupForList group = item.getModelObject();
				item.add(new CheckBox("selected", new PropertyModel<Boolean>(group, "selected")){
					@Override
					public void onSelectionChanged(Object newSelection) {
						boolean flag = ((Boolean) newSelection).booleanValue();
//						FIXME selection
						
					}			
					
				});
				item.add(new Label("name-group",group.getName()));
				item.add(new Label("ssys-group",group.getSubsystemName()));
			}
			
		};
	
		Form form = new Form("form") {

			@Override
			protected void onSubmit() {
				final ArrayList<UIGroupForList> uiWrap = new ArrayList<UIGroupForList>();
				for(UIGroupForList e : ((IndexedSortableDataProvider<UIGroupForList>)groupDataView.getDataProvider()).getDataset()){
					if(e.isSelected())uiWrap.add(e);
				}
				if (uiWrap.size() == 0)	return;
				
				this.getParent().get("confirmPanel").replaceWith(
					new ConfirmPanel("confirmPanel",
							"You are about to delete "+ uiWrap.size()
									+ " group(s) permanently?") {
						public void onConfirm() {
							for (UIGroupForList ui : uiWrap)
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
		form.add(groupDataView);
		
		form.add(groupDataView);
		form.add(new OrderByLink("order-by-groupName", "groupName", groupDataProvider));
		form.add(new OrderByLink("order-by-subsystemName", "groupSubsystem", groupDataProvider));
		form.add(new PagingNavigator("paging-navigator", groupDataView));
		
		form.add(new CheckBox("groupselector", new Model("empty")) {
			@Override
			public void onSelectionChanged(Object newSelection) {
				boolean flag = ((Boolean) newSelection).booleanValue();
				
				SortableDataProvider<UIGroupForList> list = ((SortableDataProvider<UIGroupForList>)groupDataView.getDataProvider());
				
				Iterator i = list.iterator(0, list.size());
				while(i.hasNext()){
					UIGroupForList group = (UIGroupForList)i.next();
					group.setSelected(flag);
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


@SuppressWarnings("unused")
private class GroupFilter implements Serializable {
	private UISubsystemForFilter subsystem;

	public UISubsystemForFilter getSubsystem() {
		return subsystem;
	}

	public void setSubsystem(UISubsystemForFilter subsystem) {
		this.subsystem = subsystem;
	}

}

}
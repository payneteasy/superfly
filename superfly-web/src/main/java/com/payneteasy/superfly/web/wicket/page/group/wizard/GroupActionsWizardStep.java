package com.payneteasy.superfly.web.wicket.page.group.wizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.ActionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.repeater.IndexedSortableDataProvider;

@Secured("ROLE_ADMIN")
public class GroupActionsWizardStep extends WizardStep {
	
	@SpringBean
	ActionService actionService;
	@SpringBean
	SubsystemService ssysService;
	
	private static final long serialVersionUID = 1L;

	public GroupActionsWizardStep(IModel<String> title, IModel<String> summary,
			IModel<?> model) {
		super(title, summary, model);
		
		// SORTABLE DATA PROVIDER
		final GroupWizardModel wizardModel = (GroupWizardModel)model.getObject();
				
		String[] fieldName = { "actionName" };
		final SortableDataProvider<UIActionForList> actionDataProvider = new IndexedSortableDataProvider<UIActionForList>(
				fieldName) {
						
			public Iterator<? extends UIActionForList> iterator(int first,
					int count) {
				UISubsystemForFilter subsystem = wizardModel.getGroupSubsystem();
				List<Long> subsystemId = new ArrayList<Long>();
				subsystemId.add(subsystem.getId());
				List<UIActionForList> list = actionService.getActions(first, count,
						getSortFieldIndex(), isAscending(), null, null,
						subsystem == null ? null : subsystemId);
				return list.iterator(); 
			}

			public int size() {
				UISubsystemForFilter subsystem = wizardModel.getGroupSubsystem();
				List<Long> subsystemId = new ArrayList<Long>();
				subsystemId.add(subsystem.getId());
				return actionService.getActionCount(null, null, subsystem == null ? null : subsystemId);
			}

		};


		
//		DATAVIEW
		final CheckGroup checkGroup = new CheckGroup("group", new ArrayList());
		
		final DataView<UIActionForList> actionDataView = new PagingDataView<UIActionForList>("dataView",actionDataProvider){
			@Override
			protected void populateItem(Item<UIActionForList> item) {
				final UIActionForList action = item.getModelObject();
				item.add(new Label("name",action.getName()));
				item.add(new Label("description",action.getSubsystemName()));
				item.add(new Check("selected", new PropertyModel(item.getModel(), "selected")));
			}
			
		};
	
		
		checkGroup.add(actionDataView);
		checkGroup.add(new OrderByLink("order-by-name", "actionName", actionDataProvider));
		checkGroup.add(new PagingNavigator("paging-navigator", actionDataView));
		
		checkGroup.add(new CheckGroupSelector("groupselector"));
		add(checkGroup);
	}

}

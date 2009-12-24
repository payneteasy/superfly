package com.payneteasy.superfly.web.page.group;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.payneteasy.superfly.model.ui.UIGroupForList;
import com.payneteasy.superfly.service.GroupService;
import com.payneteasy.superfly.web.page.BasePage;

public class GroupListPage extends BasePage {
	@SpringBean
	private GroupService groupService;

	@SuppressWarnings("unchecked")
	public GroupListPage() {
     List<UIGroupForList> groups = groupService.getGroups();
     ListView<UIGroupForList> listViewGroups = new ListView<UIGroupForList>("list-group",groups){

		@Override
		protected void populateItem(ListItem<UIGroupForList> item) {
			UIGroupForList groups = item.getModelObject();
			item.add(new Label("name-group",groups.getName()));
			
		}
    	 
     };
     add(listViewGroups);
	}
}

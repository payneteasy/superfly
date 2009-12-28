package com.payneteasy.superfly.web.wicket.page.action;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.service.ActionService;
import com.payneteasy.superfly.web.wicket.page.BasePage;

public class ListActionsPage extends BasePage{
	@SpringBean
	private ActionService actionService;
	
	public ListActionsPage(){
		
		List<UIActionForList> actionList = actionService.getAction();
		ListView<UIActionForList> listViewAction = new ListView<UIActionForList>("list-action",actionList){

			@Override
			protected void populateItem(ListItem<UIActionForList> item) {
				 UIActionForList action = item.getModelObject();
				 item.add(new Label("name-action",action.getName()));
				 item.add(new Label("description-action", action.getDescroption()));
				 item.add(new Label("log-action",action.isLogAction()?"yes":"NO"));
			}
		};
		add(listViewAction);
	}
}

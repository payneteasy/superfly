package com.payneteasy.superfly.web.wicket.page.group;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.service.GroupService;
import com.payneteasy.superfly.service.mapping.MappingService;
import com.payneteasy.superfly.web.wicket.component.mapping.MappingPanel;
import com.payneteasy.superfly.web.wicket.page.BasePage;

@Secured("ROLE_ADMIN")
public class ChangeGroupActionsPage extends BasePage {
	@SpringBean
	private GroupService groupService;

	public ChangeGroupActionsPage(final PageParameters parameters) {
		super(ListGroupsPage.class, parameters);
		
		final long groupId = parameters.getAsLong("gid");
		UIGroup group = groupService.getGroupById(groupId);
		add(new Label("group-name", group.getName()));
        add(new MappingPanel("mapping-panel",groupId){

			@Override
			protected List<? extends MappingService> getMappedItems(String searchLabel) {
				return groupService.getAllGroupMappedActions(0, Integer.MAX_VALUE, 5, true,groupId, searchLabel);
			}

			@Override
			protected List<? extends MappingService> getUnMappedItems(String searchLabel) {
				return groupService.getAllGroupUnMappedActions(0, Integer.MAX_VALUE, 5, true,groupId, searchLabel);
			}

			@Override
			protected void mappingProcess(long entityId, List<Long> mappedId, List<Long> unmappedId) {
				groupService.changeGroupActions(groupId, mappedId, unmappedId);
				setResponsePage(ChangeGroupActionsPage.class, parameters);
			}

			@Override
			protected boolean isVisibleSearchePanel() {
				return true;
			}

			@Override
			protected String getHeaderItemName() {
				return "Actions";
			}
        	
        });
		add(new BookmarkablePageLink<Page>("back", ListGroupsPage.class,
				parameters));
	}

	@Override
	protected String getTitle() {
		return "Change group actions";
	}
}

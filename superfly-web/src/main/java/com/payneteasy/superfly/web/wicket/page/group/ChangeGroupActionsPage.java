package com.payneteasy.superfly.web.wicket.page.group;

import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForGroup;
import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.service.GroupService;
import com.payneteasy.superfly.web.wicket.component.mapping.MappingPanel;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.utils.PageParametersBuilder;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import java.util.List;

@Secured("ROLE_ADMIN")
public class ChangeGroupActionsPage extends BasePage {
    @SpringBean
    private GroupService groupService;

    public ChangeGroupActionsPage(final PageParameters parameters) {
        super(ListGroupsPage.class, parameters);

        final long groupId = parameters.get("gid").toLong();
        UIGroup group = groupService.getGroupById(groupId);
        add(new Label("group-name", group.getName()));
        add(new MappingPanel<UIActionForCheckboxForGroup>("mapping-panel",groupId){

            @Override
            protected List<UIActionForCheckboxForGroup> getMappedItems(String searchLabel) {
                return groupService.getAllGroupMappedActions(0, Integer.MAX_VALUE, 5, true,groupId, searchLabel);
            }

            @Override
            protected List<UIActionForCheckboxForGroup> getUnMappedItems(String searchLabel) {
                return groupService.getAllGroupUnMappedActions(0, Integer.MAX_VALUE, 5, true,groupId, searchLabel);
            }

            @Override
            protected void mappingProcess(long entityId, List<Long> mappedId, List<Long> unmappedId) {
                groupService.changeGroupActions(groupId, mappedId, unmappedId);
                setResponsePage(ChangeGroupActionsPage.class, parameters);
            }

            @Override
            protected String getHeaderItemName() {
                return "Actions";
            }
        });
        add(new BookmarkablePageLink<Page>("back-to-view", ViewGroupPage.class, PageParametersBuilder.fromPair("gid", group.getId())));
        add(new BookmarkablePageLink<Page>("back-to-list", ListGroupsPage.class, parameters));
    }

    @Override
    protected String getTitle() {
        return "Change group actions";
    }
}

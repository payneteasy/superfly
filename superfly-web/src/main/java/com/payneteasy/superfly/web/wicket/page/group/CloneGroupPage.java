package com.payneteasy.superfly.web.wicket.page.group;

import com.payneteasy.superfly.model.ui.group.UICloneGroupRequest;
import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.service.GroupService;
import com.payneteasy.superfly.web.wicket.component.field.LabelTextFieldRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelValueRow;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import java.io.Serializable;

@Secured("ROLE_ADMIN")
public class CloneGroupPage extends BasePage {
	@SpringBean
	private GroupService groupService;

	@Override
	protected String getTitle() {
		return "Clone group";
	}

	@SuppressWarnings("serial")
	public CloneGroupPage(PageParameters param) {
		super(ListGroupsPage.class, param);
		
		final Long sourceId = param.get("sid").toLong();
		
		final UIGroup sourceGroup = groupService.getGroupById(sourceId);
		GroupModel groupModel = new GroupModel();
			
		Form<GroupModel> form = new Form<GroupModel>("form", new Model<GroupModel>(groupModel)) {
			@Override
			protected void onSubmit() {
				UICloneGroupRequest request = new UICloneGroupRequest();
				request.setNewGroupName(getModelObject().getName());
				request.setSourceGroupId(sourceGroup.getId());
				groupService.cloneGroup(request);
				setResponsePage(ListGroupsPage.class);
			}

		};
		add(form);
		
		form.add(new LabelValueRow<String>("source", new Model(sourceGroup.getLabel()), "group.clone.source"));
		form.add(new LabelTextFieldRow<String>(groupModel, "name", "group.clone.name", true));
		form.add(new BookmarkablePageLink<Page>("cancel", ListGroupsPage.class));
	}
	
	private class GroupModel implements Serializable{
		private static final long serialVersionUID = 1L;
		private String name;

		public String getName() {
			return name;
		}

		@SuppressWarnings("unused")
		public void setName(String name) {
			this.name = name;
		}
		
	}

}

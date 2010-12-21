package com.payneteasy.superfly.web.wicket.page.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import com.payneteasy.superfly.model.ui.role.UIRoleForList;
import com.payneteasy.superfly.model.ui.role.UIRoleWithActions;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.model.ui.user.UIUserAddSubsystemWithRole;
import com.payneteasy.superfly.model.ui.user.UIUserWithRolesAndActions;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.component.RoleInCreateUserChoiceRender;
import com.payneteasy.superfly.web.wicket.component.SubsystemInCreateUserChoiceRender;
import com.payneteasy.superfly.web.wicket.component.field.LabelDropDownChoiceRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelValueRow;
import com.payneteasy.superfly.web.wicket.page.BasePage;

@Secured("ROLE_ADMIN")
public class AddSubsystemWithRolePage extends BasePage {
	@SpringBean
	private UserService userService;
	@SpringBean
	private RoleService roleService;
	@SpringBean
	private SubsystemService subsystemService;
	private boolean isVisible=true;

	public AddSubsystemWithRolePage(PageParameters params) {
		super(ListUsersPage.class, params);
		final long userId = params.getAsLong("userId");
		UIUser user = userService.getUser(userId);
		
		add(new LabelValueRow<String>("user-name", new Model(user.getUsername()), "user.name"));
		
		WebMarkupContainer container = new WebMarkupContainer("container");
		add(container);
		
		List<UISubsystemForList> listSub = subsystemService.getSubsystems();
		
		UIUserWithRolesAndActions user1 = userService.getUserRoleActions(userId, null, null, null);
		
		final List<UIRoleWithActions> roleWithAction = user1.getRoles();
		
		final SortRoleOfSubsystem sort = new SortRoleOfSubsystem();
		sort.setRoleWithAction(roleWithAction);
		List<String> oldSubName = sort.getSubsystemsName();
		List<UISubsystemForList> oldSub = new ArrayList<UISubsystemForList>();
		List<UISubsystemForList> newSub = new ArrayList<UISubsystemForList>();
		for (UISubsystemForList ui : listSub) {
			for (String sub : oldSubName) {
				if (sub.equals(ui.getName())) {
					oldSub.add(ui);
				}
			}
		}
		for (UISubsystemForList old : listSub) {
			if (!oldSub.contains(old)) {
				newSub.add(old);
			}
		}
		
        if(newSub.isEmpty()){
        	isVisible=false;
        }
        container.setVisible(isVisible);
        
		for (UISubsystemForList sub : newSub) {
			List<Long> listIdsub = new ArrayList<Long>();
			listIdsub.add(sub.getId());
			List<UIRoleForList> listRole = roleService.getRoles(0, Integer.MAX_VALUE, 1, true, null, listIdsub);
			modelsMap.put(sub, listRole);
		}

		// models for DropDrownChoice
		IModel<List<? extends UISubsystemForList>> makeChoices = new AbstractReadOnlyModel<List<? extends UISubsystemForList>>() {
			@Override
			public List<UISubsystemForList> getObject() {
				Set<UISubsystemForList> keys = modelsMap.keySet();
				List<UISubsystemForList> list = new ArrayList<UISubsystemForList>(keys);
				return list;
			}

		};
		final IModel<List<? extends UIRoleForList>> modelChoices = new AbstractReadOnlyModel<List<? extends UIRoleForList>>() {
			@Override
			public List<UIRoleForList> getObject() {
				List<UIRoleForList> models = modelsMap.get(subsystem);
				if (models == null) {
					models = Collections.emptyList();
				}
				return models;
			}

		};
		Form<UIUserAddSubsystemWithRole> form = new Form<UIUserAddSubsystemWithRole>("form");
		container.add(form);

		LabelDropDownChoiceRow<UISubsystemForList> makes = new LabelDropDownChoiceRow<UISubsystemForList>("subsystem", this, "user.create.choice-subsystem", makeChoices, new SubsystemInCreateUserChoiceRender());
		makes.getDropDownChoice().setRequired(true);
		
		final LabelDropDownChoiceRow<UIRoleForList> models = new LabelDropDownChoiceRow<UIRoleForList>("role", new Model<UIRoleForList>(), "user.create.choice-roles", modelChoices, new RoleInCreateUserChoiceRender());
		models.getDropDownChoice().setRequired(true);
		models.setOutputMarkupId(true);
		
		form.add(makes);
		form.add(models);
		makes.getDropDownChoice().add(new AjaxFormComponentUpdatingBehavior("onchange"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.addComponent(models);
			}
			
		});
		form.add(new Button("add-sub") {

			@Override
			public void onSubmit() {
				UIRoleForList role = models.getDropDownChoice().getModelObject();
				userService.addSubsystemWithRole(userId, role.getId());
				PageParameters param = new PageParameters();
				param.add("userId", String.valueOf(userId));
				getRequestCycle().setResponsePage(UserDetailsPage.class, param);
				getRequestCycle().setRedirect(true);

			}

		});
		final PageParameters param = new PageParameters();
		param.add("userId", String.valueOf(userId));
		form.add(new BookmarkablePageLink<Page>("cancel", UserDetailsPage.class, param));
		
		WebMarkupContainer noMoreSubContainer = new WebMarkupContainer("no-more-sub-container");
		noMoreSubContainer.setVisible(!isVisible);
		noMoreSubContainer.add(new BookmarkablePageLink<Page>("back", UserDetailsPage.class, param));
		add(noMoreSubContainer);
	}

	@Override
	protected String getTitle() {
		return "Add subsystem with role";
	}

	private final Map<UISubsystemForList, List<UIRoleForList>> modelsMap = new HashMap<UISubsystemForList, List<UIRoleForList>>(); // map:company->model
	private UIRoleForList role;
	private UISubsystemForList subsystem;

	public UIRoleForList getRole() {
		return role;
	}

	public void setRole(UIRoleForList role) {
		this.role = role;
	}

	public UISubsystemForList getSubsystem() {
		return subsystem;
	}

	public void setSubsystem(UISubsystemForList subsystem) {
		this.subsystem = subsystem;
	}
}

package com.payneteasy.superfly.web.wicket.page.user;

import org.apache.wicket.Page;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.password.PasswordGenerator;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.page.BasePage;

@Secured("ROLE_ADMIN")
public class ResetPasswordUserPage extends BasePage {
	@SpringBean
	private ResetPasswordStrategy resetPasswordStrategy;
	
	@SpringBean
	private UserService userService;
	@SpringBean
	private PasswordGenerator passwordGenerator;
	
	public ResetPasswordUserPage(final PageParameters parameters) {
		super(ListUsersPage.class, parameters);
		
		final long userId = parameters.get("userId").toLong();
		final UIUser user = userService.getUser(userId);
//		System.out.println("POLICY NAME "+resetPasswordStrategy.getPolicyName());
		Form<Void> form = new Form<Void>("form");
		add(form);
		final Label label = new Label("generated-password", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				setNewPassword(passwordGenerator.generate());
				return getNewPassword();
			}
		});
		label.setOutputMarkupId(true);
		form.add(label);
		form.add(new IndicatingAjaxLink<String>("generateNewPassword") {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget aTarget) {
				setNewPassword(passwordGenerator.generate());
				aTarget.add(label);
			}
		});
		form.add(new BookmarkablePageLink<Page>("cancel", ListUsersPage.class));
        form.add(new Button("reset-button"){

			@Override
			public void onSubmit() {
				resetPasswordStrategy.resetPassword(userId, user.getUsername(), newPassword);
				setResponsePage(ListUsersPage.class);
			}
        	
        });
	}

	@Override
	protected String getTitle() {
		return "Reset password";
	}

	private String newPassword;

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

}

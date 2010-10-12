package com.payneteasy.superfly.web.wicket.page.user;

import java.util.Random;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.page.BasePage;

@Secured("ROLE_ADMIN")
public class ResetPasswordUserPage extends BasePage {
	@SpringBean
	private ResetPasswordStrategy resetPasswordStrategy;
	
	@SpringBean
	private UserService userService;
	
	public ResetPasswordUserPage(final PageParameters parameters) {
		final long userId = parameters.getAsLong("userId");
		final UIUser user = userService.getUser(userId);
		System.out.println("POLICY NAME "+resetPasswordStrategy.getPolicyName());
		Form<Void> form = new Form<Void>("form");
		add(form);
		final Label label = new Label("generated-password", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				setNewPassword(getRandomString(6));
				return getNewPassword();
			}
		});
		label.setOutputMarkupId(true);
		form.add(label);
		form.add(new IndicatingAjaxLink<String>("generateNewPassword") {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget aTarget) {
				setNewPassword(getRandomString(6));
				aTarget.addComponent(label);
			}
		});
		form.add(new BookmarkablePageLink<Page>("cancel", ListUsersPage.class));
        form.add(new Button("reset-button"){

			@Override
			public void onSubmit() {
				resetPasswordStrategy.resetPassword(userId, user.getName(), newPassword);
				setResponsePage(ListUsersPage.class);
			}
        	
        });
	}

	@Override
	protected String getTitle() {
		return "Reset password";
	}

	private static final String charset = "0123456789abcdefghijklmnopqrstuvwxyz";

	public String getRandomString(int length) {
		Random rand = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int pos = rand.nextInt(charset.length());
			sb.append(charset.charAt(pos));
		}
		return sb.toString();
	}

	private String newPassword;

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

}

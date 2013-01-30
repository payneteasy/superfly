package com.payneteasy.superfly.web.wicket.page.subsystem;

import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForFilter;
import com.payneteasy.superfly.service.SmtpServerService;
import com.payneteasy.superfly.web.wicket.component.field.LabelDropDownChoiceRow;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.UrlValidator;
import org.springframework.security.access.annotation.Secured;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.field.LabelCheckBoxRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelTextFieldRow;
import com.payneteasy.superfly.web.wicket.page.BasePage;

import java.util.List;

@Secured("ROLE_ADMIN")
public class EditSubsystemPage extends BasePage {
	@SpringBean
	private SubsystemService subsystemService;
    @SpringBean
    private SmtpServerService smtpServerService;

	public EditSubsystemPage(PageParameters parameters) {
		super(ListSubsystemsPage.class, parameters);
		
		long subsystemId = parameters.getAsLong("id", -1L);
		final UISubsystem subsystem = subsystemService.getSubsystem(subsystemId);
		
		Form<UISubsystem> form = new Form<UISubsystem>("form", new CompoundPropertyModel<UISubsystem>(subsystem)) {

			@Override
			protected void onSubmit() {
				subsystemService.updateSubsystem(subsystem);
				setResponsePage(ListSubsystemsPage.class);
			}

		};
		add(form);
		
        form.add(new LabelTextFieldRow<UISubsystem>(subsystem,"name","subsystem.edit.name",true));
		
		LabelTextFieldRow<String> callbackInformation = new LabelTextFieldRow<String>(subsystem, "callbackInformation", "subsystem.edit.callback",true);
		callbackInformation.getTextField().add(new UrlValidator(new String[] {"http", "https"}));
		form.add(callbackInformation);

        LabelTextFieldRow<String> landingUrlRow = new LabelTextFieldRow<String>(subsystem, "landingUrl", "subsystem.edit.landingUrl",true);
        landingUrlRow.getTextField().add(new UrlValidator(new String[] {"http", "https"}));
        form.add(landingUrlRow);
		
		form.add(new LabelCheckBoxRow("allowListUsers", subsystem, "subsystem.edit.allow-list-users"));

        IModel<List<UISmtpServerForFilter>> smtpServersModel = new LoadableDetachableModel<List<UISmtpServerForFilter>>() {
            @Override
            protected List<UISmtpServerForFilter> load() {
                return smtpServerService.getSmtpServersForFilter();
            }
        };
        form.add(new LabelDropDownChoiceRow<UISmtpServerForFilter>("smtpServer", subsystem, "subsystem.smtpServer",
                smtpServersModel, new IChoiceRenderer<UISmtpServerForFilter>() {
            public Object getDisplayValue(UISmtpServerForFilter server) {
                return server == null ? "" : server.getName();
            }

            public String getIdValue(UISmtpServerForFilter server, int index) {
                return server == null ? "" : String.valueOf(server.getId());
            }
        }, true));
		
		form.add(new Button("form-submit"));
		form.add(new BookmarkablePageLink<Page>("cancel",ListSubsystemsPage.class));
	}

	@Override
	protected String getTitle() {
		return "Edit subsystem";
	}

}

package com.payneteasy.superfly.web.page.subsystem;

import org.apache.wicket.PageParameters;

import com.payneteasy.superfly.web.page.BasePage;

public class EditSubsytemPage extends BasePage {
	public EditSubsytemPage(PageParameters parameters) {
       this(parameters.getAsLong("id"));
	}

	public EditSubsytemPage(Long asLong) {
		
	}
}

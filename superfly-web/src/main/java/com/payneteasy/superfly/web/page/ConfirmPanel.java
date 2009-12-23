package com.payneteasy.superfly.web.page;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

public abstract class ConfirmPanel extends Panel{
	public ConfirmPanel(String id, String message) {
	    super(id);
	    Form cform = new Form("cform");
	    cform.add(new Label("message", message));
	    cform.add(new Button("confirm") {
	      @Override
	      public void onSubmit() {
	        onConfirm();
	      }
	    });
	    cform.add(new Button("cancel") {
	      @Override
	      public void onSubmit() {
	        onCancel();
	      }
	    });
	    add(cform);
	  }
	  protected abstract void onCancel();
	  protected abstract void onConfirm();
}

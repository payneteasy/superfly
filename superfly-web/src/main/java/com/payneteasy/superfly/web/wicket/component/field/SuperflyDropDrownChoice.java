package com.payneteasy.superfly.web.wicket.component.field;

import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;

public class SuperflyDropDrownChoice<T> extends DropDownChoice<T> {
	private String validatorKeyPrefix;

	public SuperflyDropDrownChoice(String id, List<? extends T> choices) {
		super(id, choices);
		setOutputMarkupId(true);
		setMarkupId(id);
	}

	public SuperflyDropDrownChoice(String id, List<? extends T> data, IChoiceRenderer<? super T> iChoiceRenderer) {
		super(id, data, iChoiceRenderer);
		setOutputMarkupId(true);
		setMarkupId(id);
	}

	public SuperflyDropDrownChoice(String id, IModel<T> tiModel, List<? extends T> choices) {
		super(id, tiModel, choices);
		setOutputMarkupId(true);
		setMarkupId(id);
	}

	public SuperflyDropDrownChoice(String id, IModel<T> tiModel, List<? extends T> data, IChoiceRenderer<? super T> iChoiceRenderer) {
		super(id, tiModel, data, iChoiceRenderer);
		setOutputMarkupId(true);
		setMarkupId(id);
	}

	public SuperflyDropDrownChoice(String id, IModel<? extends List<? extends T>> choices) {
		super(id, choices);
		setOutputMarkupId(true);
		setMarkupId(id);
	}

	public SuperflyDropDrownChoice(String id, IModel<T> tiModel, IModel<? extends List<? extends T>> choices) {
		super(id, tiModel, choices);
		setOutputMarkupId(true);
		setMarkupId(id);
	}

	public SuperflyDropDrownChoice(String id, IModel<? extends List<? extends T>> choices, IChoiceRenderer<? super T> iChoiceRenderer) {
		super(id, choices, iChoiceRenderer);
		setOutputMarkupId(true);
		setMarkupId(id);
	}

	public SuperflyDropDrownChoice(String id, IModel<T> tiModel, IModel<? extends List<? extends T>> choices,
			IChoiceRenderer<? super T> iChoiceRenderer) {
		super(id, tiModel, choices, iChoiceRenderer);
		setOutputMarkupId(true);
		setMarkupId(id);
	}

	@Override
	public String getValidatorKeyPrefix() {
		return validatorKeyPrefix;
	}

	public void setValidatorKeyPrefix(String validatorKeyPrefix) {
		this.validatorKeyPrefix = validatorKeyPrefix;
	}
}

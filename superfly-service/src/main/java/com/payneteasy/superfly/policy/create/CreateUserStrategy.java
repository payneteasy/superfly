package com.payneteasy.superfly.policy.create;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.model.ui.user.UIUserForCreate;

public interface CreateUserStrategy {
	RoutineResult createUser(UIUserForCreate createUser);
}

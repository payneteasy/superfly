package com.payneteasy.superfly.register;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.UserRegisterRequest;

public interface RegisterUserStrategy {
	RoutineResult registerUser(UserRegisterRequest registerUser);
}

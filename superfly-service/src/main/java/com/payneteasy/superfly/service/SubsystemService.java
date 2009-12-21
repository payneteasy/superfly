package com.payneteasy.superfly.service;

import java.util.List;

import com.payneteasy.superfly.model.ui.UISubsystem;
import com.payneteasy.superfly.model.ui.UISubsystemForList;

public interface SubsystemService {
	List<UISubsystemForList> getSubsystems();
	void createSubsystem(UISubsystem subsystem);
	void updateSubsystem(UISubsystem subsystem);
	void deleteSubsystem(Long subsystemId);
}

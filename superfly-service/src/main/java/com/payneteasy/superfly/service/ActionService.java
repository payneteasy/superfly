package com.payneteasy.superfly.service;

import java.util.List;

import com.payneteasy.superfly.model.ui.action.UIActionForList;

public interface ActionService {
   List<UIActionForList> getAction(List<Long> subsystemIds);
}


package com.payneteasy.superfly.service;

import com.payneteasy.superfly.model.Event;

import java.util.Date;
import java.util.List;

/**
 * Service to work with events.
 * 
 */
public interface EventService {
    List<Event> getEvents(Date lastEventTime, long waitTime);
}

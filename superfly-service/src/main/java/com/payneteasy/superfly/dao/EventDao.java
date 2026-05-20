package com.payneteasy.superfly.dao;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.Event;

import java.util.Date;
import java.util.List;

/**
 * DAO to work with events.
 */
public interface EventDao {

    @AStoredProcedure(name = "ui_get_events")
    List<Event> getEvents(Date lastEventTime, int limit);
    
}

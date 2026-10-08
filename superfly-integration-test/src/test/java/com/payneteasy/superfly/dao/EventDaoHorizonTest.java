package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.model.Event;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Events younger than the stability horizon (5 s) must stay invisible: a younger committed event
 * must not move a client's cursor past an older one that is still held by an open transaction.
 */
public class EventDaoHorizonTest extends AbstractDaoTest {
    private static final String EVENT_CODE = "EVENT_DAO_HORIZON_TEST";
    private static final String SUBSYSTEM = "event-dao-horizon-test";

    private EventDao eventDao;
    private JdbcTemplate jdbcTemplate;
    private long subsystemId;

    @Autowired
    public void setEventDao(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Before
    public void setUp() {
        cleanUp();
        jdbcTemplate.update("insert into event_types (event_code, event_name) values (?, ?)", EVENT_CODE, "event dao horizon test");
        jdbcTemplate.update("insert into subsystems (subsystem_name, landing_url, subsystem_url, subsystem_title) values (?, 'http://localhost', 'http://localhost', ?)", SUBSYSTEM, SUBSYSTEM);
        subsystemId = jdbcTemplate.queryForObject("select ssys_id from subsystems where subsystem_name = ?", Long.class, SUBSYSTEM);
    }

    @After
    public void cleanUp() {
        jdbcTemplate.update("delete from events where event_type_id in (select event_type_id from event_types where event_code = ?)", EVENT_CODE);
        jdbcTemplate.update("delete from event_types where event_code = ?", EVENT_CODE);
        jdbcTemplate.update("delete from subsystems where subsystem_name = ?", SUBSYSTEM);
    }

    @Test
    public void testEventYoungerThanHorizonIsNotReturned() {
        long old = createEvent("old", 60);
        createEvent("young", 1);

        assertEquals(List.of(old), ids(eventDao.getEvents(null, 10, SUBSYSTEM)));
    }

    @Test
    public void testLastEventIdSkipsEventsYoungerThanHorizon() {
        long old = createEvent("old", 60);
        createEvent("young", 1);

        assertEquals(old, eventDao.getLastEventId(SUBSYSTEM));
    }

    @Test
    public void testLastEventIdIsZeroWhenAllEventsAreYoung() {
        createEvent("young", 1);

        assertEquals(0L, eventDao.getLastEventId(SUBSYSTEM));
    }

    private static List<Long> ids(List<Event> events) {
        return events.stream().map(Event::getEventId).collect(Collectors.toList());
    }

    private long createEvent(String data, int ageSeconds) {
        jdbcTemplate.update("insert into events (event_time, event_type_id, event_data, subsystem_id) "
                + "select now() - interval ? second, event_type_id, ?, ? from event_types where event_code = ?",
                ageSeconds, data, subsystemId, EVENT_CODE);
        return jdbcTemplate.queryForObject("select event_id from events where event_data = ? and subsystem_id = ?", Long.class, data, subsystemId);
    }
}

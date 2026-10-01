package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.model.Event;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventDaoTest extends AbstractDaoTest {
    private static final String EVENT_CODE = "EVENT_DAO_TEST";
    private static final String SUBSYSTEM_A = "event-dao-test-a";
    private static final String SUBSYSTEM_B = "event-dao-test-b";

    private EventDao eventDao;
    private JdbcTemplate jdbcTemplate;

    private List<Long> idsA;
    private List<Long> idsB;

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
        jdbcTemplate.update("insert into event_types (event_code, event_name) values (?, ?)", EVENT_CODE, "event dao test");
        long subsystemA = createSubsystem(SUBSYSTEM_A);
        long subsystemB = createSubsystem(SUBSYSTEM_B);
        // all events share the same event_time: the old time-based cursor could not tell them apart
        idsA = new ArrayList<>();
        idsB = new ArrayList<>();
        idsA.add(createEvent("a1", subsystemA));
        idsA.add(createEvent("a2", subsystemA));
        idsB.add(createEvent("b1", subsystemB));
        idsA.add(createEvent("a3", subsystemA));
        idsA.add(createEvent("a4", subsystemA));
        idsA.add(createEvent("a5", subsystemA));
    }

    @After
    public void cleanUp() {
        jdbcTemplate.update("delete from events where event_type_id in (select event_type_id from event_types where event_code = ?)", EVENT_CODE);
        jdbcTemplate.update("delete from event_types where event_code = ?", EVENT_CODE);
        jdbcTemplate.update("delete from subsystems where subsystem_name in (?, ?)", SUBSYSTEM_A, SUBSYSTEM_B);
    }

    @Test
    public void testPagingByEventIdDoesNotLoseOrRepeatEventsWithSameTime() {
        List<Long> seen = new ArrayList<>();
        Long cursor = null;
        int pages = 0;
        while (true) {
            List<Long> page = ids(eventDao.getEvents(cursor, 2, SUBSYSTEM_A));
            if (page.isEmpty()) {
                break;
            }
            assertTrue("page must not exceed the limit: " + page, page.size() <= 2);
            seen.addAll(page);
            cursor = page.get(page.size() - 1);
            pages++;
            assertTrue("paging does not terminate", pages < 10);
        }
        assertEquals(idsA, seen);
        assertEquals(3, pages);
    }

    @Test
    public void testEventsOfOtherSubsystemAreNotVisible() {
        assertEquals(idsB, ids(eventDao.getEvents(null, 10, SUBSYSTEM_B)));
        assertEquals(idsA, ids(eventDao.getEvents(null, 10, SUBSYSTEM_A)));
    }

    @Test
    public void testCursorIsExclusive() {
        Long last = idsA.get(idsA.size() - 1);
        assertTrue(eventDao.getEvents(last, 10, SUBSYSTEM_A).isEmpty());
    }

    private static List<Long> ids(List<Event> events) {
        return events.stream().map(Event::getEventId).collect(Collectors.toList());
    }

    private long createSubsystem(String name) {
        jdbcTemplate.update("insert into subsystems (subsystem_name, landing_url, subsystem_url, subsystem_title) values (?, 'http://localhost', 'http://localhost', ?)", name, name);
        return jdbcTemplate.queryForObject("select ssys_id from subsystems where subsystem_name = ?", Long.class, name);
    }

    private long createEvent(String data, long subsystemId) {
        jdbcTemplate.update("insert into events (event_time, event_type_id, event_data, subsystem_id) "
                + "select '2020-01-01 00:00:00', event_type_id, ?, ? from event_types where event_code = ?", data, subsystemId, EVENT_CODE);
        return jdbcTemplate.queryForObject("select event_id from events where event_data = ? and subsystem_id = ?", Long.class, data, subsystemId);
    }
}

package com.payneteasy.superfly.model;

import javax.persistence.Column;
import java.util.Date;

public class Event {
    private Long eventId;
    private Date eventTime;
    private String eventTypeCode;
    private String eventData;

    @Column(name = "event_id")
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    @Column(name = "event_time")
    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    @Column(name = "event_type_code")
    public String getEventTypeCode() {
        return eventTypeCode;
    }

    public void setEventTypeCode(String eventTypeCode) {
        this.eventTypeCode = eventTypeCode;
    }

    @Column(name = "event_data")
    public String getEventData() {
        return eventData;
    }

    public void setEventData(String eventData) {
        this.eventData = eventData;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId=" + eventId +
                ", eventTime=" + eventTime +
                ", eventTypeCode='" + eventTypeCode + '\'' +
                ", eventData='" + eventData + '\'' +
                '}';
    }
}

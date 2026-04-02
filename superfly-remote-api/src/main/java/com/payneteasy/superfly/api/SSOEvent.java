package com.payneteasy.superfly.api;

import java.io.Serializable;
import java.util.Date;

/**
 * SSO event
 */
public class SSOEvent implements Serializable {
    private static final long serialVersionUID = 2939579042187840631L;
    private String eventData;
    private Long eventId;
    private String eventTypeCode;
    private Date eventTime;

    public SSOEvent(Long eventId, Date eventTime, String eventTypeCode, String eventData) {
        this.eventId = eventId;
        this.eventTime = eventTime;
        this.eventTypeCode = eventTypeCode;
        this.eventData = eventData;
    }

    public String getEventData() {
        return eventData;
    }

    public void setEventData(String eventData) {
        this.eventData = eventData;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventTypeCode() {
        return eventTypeCode;
    }

    public void setEventTypeCode(String eventTypeCode) {
        this.eventTypeCode = eventTypeCode;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public String toString() {
        return "SSOEvent{" +
                "eventData='" + eventData + '\'' +
                ", eventId=" + eventId +
                ", eventTypeCode='" + eventTypeCode + '\'' +
                ", eventTime=" + eventTime +
                '}';
    }
}

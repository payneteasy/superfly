package com.payneteasy.superfly.api;

import com.google.gson.Gson;
import com.payneteasy.superfly.api.request.GetEventsRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GetEventsRequestSerializationTest {

    private final Gson gson = new Gson();

    @Test
    public void lastEventIdRoundTripsAsJsonNumber() {
        GetEventsRequest request = GetEventsRequest.builder().lastEventId(9_007_199_254_740_993L).waitTimeMs(5).build();

        String json = gson.toJson(request);

        assertTrue(json, json.contains("\"lastEventId\":9007199254740993"));
        assertFalse(json, json.contains("lastEventTime"));
        assertEquals(request, gson.fromJson(json, GetEventsRequest.class));
    }

    @Test
    public void missingLastEventIdMeansFromTheStart() {
        assertEquals(null, gson.fromJson("{\"waitTimeMs\":0}", GetEventsRequest.class).getLastEventId());
    }
}

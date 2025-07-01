package com.payneteasy.superfly.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.payneteasy.superfly.api.serialization.SSORoleActionsMapAdapter;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SSOUserSerializationTest {

    private static Gson gson;

    @BeforeClass
    public static void setUp() {
        gson = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<Map<SSORole, SSOAction[]>>() {}.getType(), new SSORoleActionsMapAdapter())
                .create();
    }

    @Test
    public void testSsoUserDeserialization() {
        String json = "{\n" +
                "  \"name\" : \"su\",\n" +
                "  \"sessionId\" : \"11\",\n" +
                "  \"otpType\" : \"GOOGLE_AUTH\",\n" +
                "  \"isOtpOptional\" : true,\n" +
                "  \"actionsMap\" : {\n" +
                "    \"admin\" : [ {\n" +
                "      \"name\" : \"action_temp_password\",\n" +
                "      \"loggingNeeded\" : true\n" +
                "    } ]\n" +
                "  },\n" +
                "  \"preferences\" : { }\n" +
                "}";

        SSOUser ssoUser = gson.fromJson(json, SSOUser.class);

        assertNotNull(ssoUser);
        assertEquals("su", ssoUser.getName());
        assertEquals("11", ssoUser.getSessionId());
        assertEquals(OTPType.GOOGLE_AUTH, ssoUser.getOtpType());
        assertTrue(ssoUser.isOtpOptional());
        assertNotNull(ssoUser.getActionsMap());
        assertEquals(1, ssoUser.getActionsMap().size());

        SSORole expectedRole = new SSORole("admin");
        assertTrue(ssoUser.getActionsMap().containsKey(expectedRole));

        SSOAction[] adminActions = ssoUser.getActionsMap().get(expectedRole);
        assertNotNull(adminActions);
        assertEquals(1, adminActions.length);
        assertEquals("action_temp_password", adminActions[0].getName());
        assertTrue(adminActions[0].isLoggingNeeded());
    }

    @Test
    public void testSsoUserSerialization() {
        SSOAction action = new SSOAction("action_temp_password", true);

        SSORole role = new SSORole("admin");
        Map<SSORole, SSOAction[]> actionsMap = new HashMap<>();
        actionsMap.put(role, new SSOAction[]{action});

        SSOUser ssoUser = new SSOUser("su", actionsMap, Collections.emptyMap());
        ssoUser.setSessionId("11");
        ssoUser.setOtpType(OTPType.GOOGLE_AUTH);
        ssoUser.setOtpOptional(true);

        String json = gson.toJson(ssoUser);

        // Проверяем, что json содержит нужные элементы
        assertTrue(json.contains("\"name\":\"su\""));
        assertTrue(json.contains("\"sessionId\":\"11\""));
        assertTrue(json.contains("\"otpType\":\"GOOGLE_AUTH\""));
        assertTrue(json.contains("\"isOtpOptional\":true"));
        assertTrue(json.contains("\"admin\":[{\"name\":\"action_temp_password\",\"loggingNeeded\":true}]"));
    }
}

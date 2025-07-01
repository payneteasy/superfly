package com.payneteasy.superfly.api.serialization;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter for proper serialization and deserialization of Map<SSORole, SSOAction[]>
 * Uses the role name as the JSON key
 */
public class SSORoleActionsMapAdapter extends TypeAdapter<Map<SSORole, SSOAction[]>> {

    private final Gson gson = new Gson();

    @Override
    public void write(JsonWriter out, Map<SSORole, SSOAction[]> value) throws IOException {
        out.beginObject();
        if (value != null) {
            for (Map.Entry<SSORole, SSOAction[]> entry : value.entrySet()) {
                out.name(entry.getKey().getName());
                gson.toJson(entry.getValue(), SSOAction[].class, out);
            }
        }
        out.endObject();
    }

    @Override
    public Map<SSORole, SSOAction[]> read(JsonReader in) throws IOException {
        Map<SSORole, SSOAction[]> map = new HashMap<>();
        in.beginObject();
        while (in.hasNext()) {
            String roleName = in.nextName();
            SSORole role = new SSORole(roleName);
            SSOAction[] actions = gson.fromJson(in, SSOAction[].class);
            map.put(role, actions);
        }
        in.endObject();
        return map;
    }
}

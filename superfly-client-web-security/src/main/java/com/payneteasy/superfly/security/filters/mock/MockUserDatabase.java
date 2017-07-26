package com.payneteasy.superfly.security.filters.mock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MockUserDatabase {

    private final Map<String, String> passwordsMap;
    private final Map<String, Set<String>> actionsMap;

    private MockUserDatabase(Map<String, String> passwordsMap, Map<String, Set<String>> actionsMap) {
        this.passwordsMap = passwordsMap;
        this.actionsMap = actionsMap;
    }

    public Set<String> checkUsernameAndPassword(String aUsername, String aPassword) {
        String password = passwordsMap.get(aUsername);
        if(aPassword.equals(password)) {
            return actionsMap.get(aUsername);
        }
        return null;
    }

    public static class Builder {

        private Map<String, String> passwordsMap = new HashMap<>();
        private Map<String, Set<String>> actionsMap = new HashMap<>();
        private String currentUsername;

        public Builder addUser(String aUsername, String aPassword) {
            currentUsername = aUsername;
            passwordsMap.put(aUsername, aPassword);
            actionsMap.put(aUsername, new HashSet<String>());
            return this;
        }

        public Builder addAction(String aAction) {
            Set<String> actions = actionsMap.get(currentUsername);
            actions.add(aAction);
            return this;
        }

        public MockUserDatabase build() {
            return new MockUserDatabase(passwordsMap, actionsMap);
        }
    }
}

package com.payneteasy.superfly.spring;

/**
 * Defines current security policies.
 *
 * @author Roman Puchkovskiy
 */
public enum Policy {
        NONE("none"), PCIDSS("pcidss");

    private String identifier;

    private Policy(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    /**
     * Возвращает объект Policy по его идентификатору.
     *
     * @param identifier строковое значение идентификатора
     * @return объект Policy соответствующий идентификатору или null, если такой не найден
     */
    public static Policy fromIdentifier(String identifier) {
        if (identifier == null) {
            return null;
        }

        for (Policy policy : Policy.values()) {
            if (policy.getIdentifier().equals(identifier)) {
                return policy;
            }
        }
        return null;
    }
}

package com.payneteasy.superfly.web.spring;

import jakarta.servlet.ServletContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ApplicationParameterResolver {
    private final ServletContext servletContext;

    public ApplicationParameterResolver(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Получает значение параметра из системных свойств или контекста.
     *
     * @param paramName    Имя параметра.
     * @param defaultValue Значение по умолчанию (если параметр не найден).
     * @return Значение параметра или значение по умолчанию.
     */
    public String getParameter(String paramName, String defaultValue) {
        // 1. Пытаемся получить значение из системных свойств
        return Optional
                .ofNullable(System.getProperty(paramName))
                // 2. Если не найдено, пытаемся получить из контекста
                .orElseGet(() -> Optional
                        .ofNullable(servletContext.getInitParameter(paramName))
                        // 3. Если не найдено, возвращаем значение по умолчанию
                        .orElse(defaultValue));
    }

    /**
     * Получает значение параметра из системных свойств или контекста.
     * Если параметр не найден, выбрасывает исключение.
     *
     * @param paramName Имя параметра.
     * @return Значение параметра.
     * @throws IllegalArgumentException Если параметр не найден.
     */
    public String getRequiredParameter(String paramName) {
        String value = getParameter(paramName, null);
        if (value == null) {
            throw new IllegalArgumentException("Parameter '" + paramName + "' is required but not found.");
        }
        return value;
    }
}

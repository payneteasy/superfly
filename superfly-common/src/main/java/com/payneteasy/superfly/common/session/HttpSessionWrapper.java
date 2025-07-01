package com.payneteasy.superfly.common.session;

/**
 * Абстрактная обертка для HttpSession, не зависящая от конкретной реализации Servlet API.
 * Используется для единообразной работы с сессиями из javax.servlet и jakarta.servlet.
 * 
 * @author Разработчик
 */
public interface HttpSessionWrapper {

    /**
     * Возвращает идентификатор сессии.
     *
     * @return идентификатор сессии
     */
    String getId();

    /**
     * Возвращает атрибут из сессии.
     *
     * @param name имя атрибута
     * @return значение атрибута или null, если атрибут не найден
     */
    Object getAttribute(String name);

    /**
     * Устанавливает атрибут в сессии.
     *
     * @param name имя атрибута
     * @param value значение атрибута
     */
    void setAttribute(String name, Object value);

    /**
     * Удаляет атрибут из сессии.
     *
     * @param name имя атрибута
     */
    void removeAttribute(String name);

    /**
     * Уничтожает сессию.
     */
    void invalidate();
}

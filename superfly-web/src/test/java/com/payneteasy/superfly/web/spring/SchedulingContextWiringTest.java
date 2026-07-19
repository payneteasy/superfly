package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.notification.DefaultNotifier;
import com.payneteasy.superfly.notification.Notifier;
import com.payneteasy.superfly.notification.strategy.NotificationSendStrategy;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.UserService;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.niceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Регрессионный тест на циклическую зависимость, всплывшую при миграции
 * Quartz -> Spring {@code @Scheduled}.
 *
 * <p>Цикл был такой: {@code defaultNotifier} (конструктор) тянет
 * {@code taskScheduler}; если {@code taskScheduler}-бин объявлен в конфиге,
 * чей конструктор требует {@code SessionService}/{@code UserService}, то его
 * создание тянет {@code sessionService}, а тот через {@code setNotifier} —
 * обратно {@code defaultNotifier}, который ещё в создании.
 *
 * <p>Тест воспроизводит реальное ребро {@code sessionService -> notifier} и
 * проверяет, что контекст со scheduling-конфигами поднимается без
 * {@code BeanCurrentlyInCreationException}. Если {@code taskScheduler}-бин снова
 * переедет в сервис-зависимый конфиг — тест упадёт.
 */
public class SchedulingContextWiringTest {

    @Test
    public void contextWithSchedulingAndNotifierStartsWithoutCircularReference() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(
                    TaskSchedulerConfig.class,
                    ScheduledTasksConfig.class,
                    DefaultNotifier.class,
                    StubBeans.class
            );

            // Падало бы здесь с BeanCurrentlyInCreationException при возврате цикла.
            context.refresh();

            assertNotNull("taskScheduler должен быть создан", context.getBean(TaskScheduler.class));
            assertNotNull("defaultNotifier должен быть создан", context.getBean(DefaultNotifier.class));
            assertNotNull("notifier-зависимый sessionService должен быть создан",
                    context.getBean(SessionService.class));
            assertTrue("scheduling-конфиг должен быть в контексте",
                    context.containsBean("scheduledTasksConfig"));
        }
    }

    /**
     * Заглушки реальных зависимостей. {@code sessionService} намеренно зависит
     * от {@link Notifier}, воспроизводя ребро цикла.
     *
     * <p>НЕ помечен {@code @Configuration}/{@code @Component} специально: иначе
     * широкий продакшн-скан ({@code @ComponentScan("com.payneteasy.superfly")})
     * подхватил бы этот тестовый класс при запуске {@code Start} с тестовым
     * classpath и зарегистрировал дублирующий бин {@code userService}.
     * При явном {@code register(StubBeans.class)} в тесте Spring всё равно
     * обрабатывает {@code @Bean}-методы (lite-режим), сохраняя ребро цикла.
     */
    static class StubBeans {

        @Bean
        public SessionService sessionService(Notifier notifier) {
            // Параметр notifier воссоздаёт ребро sessionService -> notifier.
            SessionService mock = niceMock(SessionService.class);
            replay(mock);
            return mock;
        }

        @Bean
        public UserService userService() {
            UserService mock = niceMock(UserService.class);
            replay(mock);
            return mock;
        }

        @Bean
        public NotificationSendStrategy notificationSendStrategy() {
            NotificationSendStrategy mock = niceMock(NotificationSendStrategy.class);
            replay(mock);
            return mock;
        }

        /**
         * Для SpEL {@code #{contextParameters['superfly-max-sso-session-age-minutes']}}
         * в {@link ScheduledTasksConfig}.
         */
        @Bean(name = "contextParameters")
        public Map<String, Object> contextParameters() {
            Map<String, Object> params = new HashMap<>();
            params.put("superfly-max-sso-session-age-minutes", "30");
            return params;
        }
    }
}

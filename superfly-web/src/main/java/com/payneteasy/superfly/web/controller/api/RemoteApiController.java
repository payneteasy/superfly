package com.payneteasy.superfly.web.controller.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.web.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/sso.service")
public class RemoteApiController {
    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setStrictness(Strictness.STRICT)
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") // Пример настройки
            .create()
            ;

    private final Map<String, Method> requestMethods = new HashMap<>();
    private final SSOService          ssoService;

    public RemoteApiController(SSOService ssoService) {
        this.ssoService = ssoService;
        for (Method method : SSOService.class.getMethods()) {
            log.info("Register method SSOService.{}", method.getName());
            requestMethods.put(method.getName(), method);
        }
    }

    @RequestMapping({"/{methodName}"})
    ResponseEntity<?> processRequest(
            @RequestBody String body,
            @PathVariable(name = "methodName") String methodName
    ) {
        if (log.isDebugEnabled()) {
            log.debug("Request {} from {}", methodName, SecurityUtils.getUsername());
        }

        return ResponseEntity.ok(invokeServiceMethodJsonSingle(
                methodName,
                body
        ));
    }

    private Object invokeServiceMethodJsonSingle(
            String aMethodName,
            String body
    ) {
        Type   argumentType = getMethodType(aMethodName);
        Object argument     = argumentType == Void.TYPE ? null : RemoteApiController.GSON.fromJson(body, argumentType);
        if (log.isDebugEnabled()) {
            log.debug("Invoke api: SSOService.{}() request is {}",
                      aMethodName,
                      GSON.toJson(argument)
            );
        }
        return invokeMethod(aMethodName, argument);
    }

    Object invokeMethod(String aMethodName, Object aArgument) {
        Method method = requestMethods.get(aMethodName);
        try {
            return (aArgument == null && method.getParameterCount() == 0) ? method.invoke(ssoService) : method.invoke(
                    ssoService,
                    aArgument
            );
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(
                    "Cannot invoke " + method + " with arguments " + GSON.toJson(aArgument),
                    e.getTargetException()
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot invoke " + method + " with arguments " + GSON.toJson(aArgument),
                    e
            );
        }
    }

    private Type getMethodType(String aMethodName) {
        Method method = requestMethods.get(aMethodName);
        if (method == null) {
            throw new IllegalStateException("No method " + aMethodName + " found");
        }
        return method.getParameterTypes().length == 0 ? Void.TYPE : method.getParameterTypes()[0];
    }
}

package com.payneteasy.superfly.web.controller.api;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.serialization.ApiSerializationManager;
import com.payneteasy.superfly.api.serialization.ExceptionWrapper;
import com.payneteasy.superfly.web.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/sso.service")
public class RemoteApiController {
    private final Map<String, Method>     requestMethods = new HashMap<>();
    private final SSOService              ssoService;
    private final ApiSerializationManager serializationManager;

    public RemoteApiController(SSOService ssoService, ApiSerializationManager serializationManager) {
        this.ssoService = ssoService;
        this.serializationManager = serializationManager;
        for (Method method : SSOService.class.getMethods()) {
            log.info("Register method SSOService.{}", method.getName());
            requestMethods.put(method.getName(), method);
        }
    }

    @RequestMapping({"/{methodName}"})
    ResponseEntity<?> processRequest(
            @RequestBody String body,
            @PathVariable(name = "methodName") String methodName,
            @RequestHeader(name = "Content-Type", defaultValue = MediaType.APPLICATION_JSON_VALUE) String contentType,
            @RequestHeader(name = "Accept", defaultValue = MediaType.APPLICATION_JSON_VALUE) String acceptType
    ) {
        if (log.isDebugEnabled()) {
            log.debug("Request {} from {}", methodName, SecurityUtils.getUsername());
        }

        Object result           = null;
        ResponseEntity.BodyBuilder bodyBuilder = null;
        try {
            result = invokeServiceMethod(methodName, body, contentType);
            bodyBuilder = ResponseEntity.ok();
        } catch (InvocationTargetException e) {
            bodyBuilder = ResponseEntity.accepted();
            result= ExceptionWrapper.from(e.getTargetException());
        } catch (Exception e) {
            bodyBuilder = ResponseEntity.internalServerError();
            result = ExceptionWrapper.from(e);
        }

        String serializedResult = serializationManager.serialize(result, acceptType);

        return bodyBuilder
                .contentType(MediaType.parseMediaType(acceptType))
                .body(serializedResult);
    }

    private Object invokeServiceMethod(
            String aMethodName,
            String body,
            String contentType
    ) throws InvocationTargetException, IllegalAccessException {
        Type   argumentType = getMethodType(aMethodName);
        Object argument     = serializationManager.deserialize(body, argumentType, contentType);
        if (log.isDebugEnabled()) {
            log.debug("Invoke api: SSOService.{}() request is {}",
                      aMethodName,
                      serializationManager.serialize(argument)
            );
        }
        return invokeMethod(aMethodName, argument);
    }

    Object invokeMethod(String aMethodName, Object aArgument) throws InvocationTargetException, IllegalAccessException {
        Method method = requestMethods.get(aMethodName);
        return (aArgument == null && method.getParameterCount() == 0)
                    ? method.invoke(ssoService)
                    : method.invoke(ssoService, aArgument);

    }

    private Type getMethodType(String aMethodName) {
        Method method = requestMethods.get(aMethodName);
        if (method == null) {
            throw new IllegalStateException("No method " + aMethodName + " found");
        }
        return method.getParameterTypes().length == 0 ? Void.TYPE : method.getParameterTypes()[0];
    }
}

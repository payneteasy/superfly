package com.payneteasy.superfly.web.controller.api;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.serialization.ApiSerializationManager;
import com.payneteasy.superfly.api.exceptions.SsoServerException;
import com.payneteasy.superfly.api.serialization.ExceptionSerializationHelper;
import com.payneteasy.superfly.api.serialization.ExceptionWrapper;
import com.payneteasy.superfly.web.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        if (!isAcceptable(acceptType)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }

        Object result;
        ResponseEntity.BodyBuilder bodyBuilder;
        try {
            result = invokeServiceMethod(methodName, body, contentType);
            bodyBuilder = ResponseEntity.ok();
        } catch (InvocationTargetException e) {
            bodyBuilder = ResponseEntity.accepted();
            result = toClientException(methodName, e.getTargetException());
        } catch (Exception e) {
            bodyBuilder = ResponseEntity.internalServerError();
            result = toClientException(methodName, e);
        }

        return bodyBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .body(serializationManager.serialize(result, MediaType.APPLICATION_JSON_VALUE));
    }

    /**
     * Only JSON is produced, so the Accept header (a list, wildcards and q-values included) has to allow it.
     * Checked before the service method is invoked: a late failure would repeat the side effect on retry.
     */
    private static boolean isAcceptable(String acceptType) {
        try {
            List<MediaType> accepted = MediaType.parseMediaTypes(acceptType);
            return accepted.isEmpty() || accepted.stream().anyMatch(MediaType.APPLICATION_JSON::isCompatibleWith);
        } catch (InvalidMediaTypeException e) {
            return false;
        }
    }

    /**
     * Contract exceptions keep their class and message for the client; anything else (DAO errors, NPE,
     * parser failures) is replaced with an opaque errorId that is logged on the server with the cause.
     */
    private static ExceptionWrapper toClientException(String methodName, Throwable t) {
        if (ExceptionSerializationHelper.isRegistered(t.getClass())) {
            return new ExceptionWrapper(t.getClass().getName(), t.getMessage(), null);
        }
        String errorId = UUID.randomUUID().toString();
        log.error("Unhandled error in remoting for method={} errorId={}", methodName, errorId, t);
        return new ExceptionWrapper(SsoServerException.class.getName(), "Internal server error, errorId: " + errorId, null);
    }

    private Object invokeServiceMethod(
            String aMethodName,
            String body,
            String contentType
    ) throws InvocationTargetException, IllegalAccessException {
        Type   argumentType = getMethodType(aMethodName);
        Object argument     = serializationManager.deserialize(body, argumentType, contentType);
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

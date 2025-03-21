package com.payneteasy.superfly.web.conroller.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.payneteasy.superfly.api.SSOService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static com.payneteasy.superfly.web.conroller.api.ExceptionResponseModel.createErrorModel;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Controller
public class RemoteApiController {

    private static final ModelAndView ALREADY_WRITTEN = null;

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") // Пример настройки
            .create()
            ;

    private final Map<String, Method> requestMethods = new HashMap<>();
    private final SSOService          ssoService;

    public RemoteApiController(SSOService ssoService) {
        this.ssoService = ssoService;
        for (Method method : SSOService.class.getMethods()) {
            log.info("Registering method SSOService.{}", method.getName());
            requestMethods.put(method.getName(), method);
        }
    }

    @RequestMapping({"/sso.service/{methodName}"})
    public ModelAndView processRequest(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse,
            @PathVariable(name = "methodName") String methodName
    ) {
        try {
            aResponse.setContentType("application/json; charset=UTF-8");

            invokeServiceMethodJson(
                    findGson(aRequest),
                    methodName,
                    aRequest.getInputStream(),
                    aResponse.getWriter()
            );
        } catch (Exception e) {
            createErrorModel("invoke.sso.service.error", e.getLocalizedMessage());
            log.error("processRequest error", e);
        }

        return ALREADY_WRITTEN;
    }

    private Gson findGson(HttpServletRequest aRequest) {
        return GSON;
    }


    void invokeServiceMethodJson(
            Gson aGson,
            String aMethodName,
            InputStream aInput,
            Appendable writer
    ) {
        invokeServiceMethodJsonSingle(aGson, aMethodName, aInput, writer);
    }

    private void invokeServiceMethodJsonSingle(
            Gson aGson,
            String aMethodName,
            InputStream aInput,
            Appendable writer
    ) {
        Type   argumentType = getMethodType(aMethodName);
        Object argument     = argumentType == Void.TYPE ? null : aGson.fromJson(new InputStreamReader(aInput, UTF_8),
                                                                                argumentType
        );
        if (!log.isDebugEnabled()) {
            log.debug("invoke api: SSOService.{}() request is {}", aMethodName, aGson.toJson(argument));
        }
        Object result = invokeMethod(aMethodName, argument);
        aGson.toJson(result, writer);
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

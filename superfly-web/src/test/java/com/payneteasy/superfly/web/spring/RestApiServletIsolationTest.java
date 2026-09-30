package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.UserDescription;
import com.payneteasy.superfly.api.request.GetUserDescriptionRequest;
import com.payneteasy.superfly.service.RemoteAuthService;
import jakarta.servlet.http.MappingMatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockHttpServletMapping;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Регрессионный тест на уязвимость из ревью PR #125 (раздел 0): публичный сервлет
 * {@code rest-api} на {@code /sso/check/*} наследовал handler mapping root-контекста и
 * отдавал весь {@link SSOService} через {@code RemoteApiController} без аутентификации.
 *
 * <p>Сервлеты поднимаются с init-params, прочитанными из настоящего {@code web.xml},
 * и с настоящим {@code dispatcher-servlet.xml}; root-контекст содержит реальный
 * {@link WebConfig} ({@code @EnableWebMvc} + {@code RemoteApiController}), как в production.
 */
public class RestApiServletIsolationTest {

    private static final String WEBAPP = "src/main/webapp";

    private SSOService ssoService;
    private RemoteAuthService remoteAuthService;
    private AnnotationConfigWebApplicationContext root;
    private DispatcherServlet restApi;
    private DispatcherServlet remoting;

    @Before
    public void setUp() throws Exception {
        ssoService = createMock(SSOService.class);
        remoteAuthService = createMock(RemoteAuthService.class);

        MockServletContext servletContext = new MockServletContext(WEBAPP, new FileSystemResourceLoader());
        root = new AnnotationConfigWebApplicationContext();
        root.setServletContext(servletContext);
        root.register(WebConfig.class, ApiSerializationConfiguration.class);
        root.addBeanFactoryPostProcessor(beanFactory -> {
            beanFactory.registerSingleton("ssoService", ssoService);
            beanFactory.registerSingleton("remoteAuthService", remoteAuthService);
        });
        root.refresh();
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, root);

        restApi = servletFromWebXml(servletContext, "rest-api");
        remoting = servletFromWebXml(servletContext, "remoting");
    }

    @After
    public void tearDown() {
        if (restApi != null) {
            restApi.destroy();
        }
        if (remoting != null) {
            remoting.destroy();
        }
        root.close();
    }

    @Test
    public void ssoServiceIsNotReachableThroughPublicRestApiServlet() throws Exception {
        replay(ssoService, remoteAuthService);

        MockHttpServletResponse response = call(restApi, "/sso/check", "/sso.service/getUserDescription",
                "{\"username\":\"admin\"}");

        // Без хендлера Spring 6.1+ бросает NoHandlerFoundException, и root-овый GlobalExceptionHandler
        // превращает его в 500; до фикса здесь был 2xx с вызовом SSOService.
        assertTrue("status " + response.getStatus(), response.getStatus() >= 400);
        verify(ssoService);
    }

    @Test
    public void remoteAuthEndpointIsServedByRestApiServlet() throws Exception {
        replay(ssoService, remoteAuthService);

        MockHttpServletResponse response = call(restApi, "/sso/check", "/check-password/billing/admin",
                "{\"username\":\"admin\",\"passwordEncrypted\":\"x\"}");

        // 401 выдаёт сам RemoteAuthCheckController (нет Authorization) — значит маршрут жив.
        assertEquals(401, response.getStatus());
        verify(remoteAuthService);
    }

    @Test
    public void ssoServiceIsStillServedByRemotingServlet() throws Exception {
        UserDescription description = new UserDescription();
        description.setUsername("admin");
        expect(ssoService.getUserDescription(anyObject(GetUserDescriptionRequest.class))).andReturn(description);
        replay(ssoService, remoteAuthService);

        MockHttpServletResponse response = call(remoting, "/remoting", "/sso.service/getUserDescription",
                "{\"username\":\"admin\"}");

        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"admin\""));
        verify(ssoService);
    }

    private static MockHttpServletResponse call(DispatcherServlet servlet, String servletPath, String pathInfo,
                                                String json) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", servletPath + pathInfo);
        request.setServletPath(servletPath);
        request.setPathInfo(pathInfo);
        request.setHttpServletMapping(new MockHttpServletMapping(
                pathInfo.substring(1), servletPath + "/*", servlet.getServletName(), MappingMatch.PATH));
        request.setContentType("application/json");
        request.setContent(json.getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);
        return response;
    }

    private static DispatcherServlet servletFromWebXml(MockServletContext servletContext, String name) throws Exception {
        Document webXml = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new File(WEBAPP, "WEB-INF/web.xml"));
        NodeList servlets = webXml.getElementsByTagName("servlet");
        for (int i = 0; i < servlets.getLength(); i++) {
            Element servlet = (Element) servlets.item(i);
            if (!name.equals(text(servlet, "servlet-name"))) {
                continue;
            }
            assertEquals(DispatcherServlet.class.getName(), text(servlet, "servlet-class"));
            MockServletConfig config = new MockServletConfig(servletContext, name);
            NodeList params = servlet.getElementsByTagName("init-param");
            for (int j = 0; j < params.getLength(); j++) {
                Element param = (Element) params.item(j);
                config.addInitParameter(text(param, "param-name"), text(param, "param-value"));
            }
            DispatcherServlet dispatcher = new DispatcherServlet();
            dispatcher.init(config);
            return dispatcher;
        }
        throw new AssertionError("servlet " + name + " not found in web.xml");
    }

    private static String text(Element parent, String tag) {
        return parent.getElementsByTagName(tag).item(0).getTextContent().trim();
    }
}

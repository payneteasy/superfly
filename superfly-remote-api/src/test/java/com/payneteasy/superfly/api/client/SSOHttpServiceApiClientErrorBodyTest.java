package com.payneteasy.superfly.api.client;

import com.payneteasy.http.client.api.HttpHeader;
import com.payneteasy.http.client.api.HttpRequest;
import com.payneteasy.http.client.api.HttpRequestParameters;
import com.payneteasy.http.client.api.HttpResponse;
import com.payneteasy.http.client.api.IHttpClient;
import com.payneteasy.superfly.api.exceptions.SsoServerException;
import com.payneteasy.superfly.api.request.AuthenticateRequest;
import com.payneteasy.superfly.api.serialization.ApiSerializationManager;
import org.easymock.EasyMock;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class SSOHttpServiceApiClientErrorBodyTest {

    @Test
    public void errorBodyInExceptionMessageIsTruncated() throws Exception {
        IHttpClient httpClient = EasyMock.createMock(IHttpClient.class);
        String body = "x".repeat(150) + "y".repeat(500);
        expect(httpClient.send(anyObject(HttpRequest.class), anyObject(HttpRequestParameters.class)))
                .andReturn(new HttpResponse(500, "Error", List.of(new HttpHeader("Content-Type", "text/plain")),
                        body.getBytes(StandardCharsets.UTF_8)));
        replay(httpClient);

        SSOClientConfig config = SSOClientConfig.builder()
                .baseUrl("https://test.example.com/superfly")
                .subsystemName("test-subsystem")
                .defaultParameters(HttpRequestParameters.builder().build())
                .build();
        SSOHttpServiceApiClient client = new SSOHttpServiceApiClient(httpClient, config, new ApiSerializationManager());

        SsoServerException e = assertThrows(SsoServerException.class,
                () -> client.authenticate(new AuthenticateRequest("user", "password")));

        assertTrue(e.getMessage().contains("x".repeat(150) + "y".repeat(50)));
        assertFalse(e.getMessage().contains("y".repeat(51)));
    }
}

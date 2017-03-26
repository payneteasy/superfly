package com.payneteasy.superfly.client.opt;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SslTestMain {
    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("test-ssl.xml");
        HttpClient httpClient = (HttpClient) applicationContext.getBean("httpClient");
        HttpMethod method = new PostMethod("/superfly/remoting/sso.service");
        httpClient.executeMethod(method);
        System.out.println(method.getStatusCode());
        System.out.println(method.getResponseHeader("Location"));
        System.out.println(method.getResponseBodyAsString());
    }
}

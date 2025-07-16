package com.payneteasy.superfly;

import org.eclipse.jetty.util.ssl.SniX509ExtendedKeyManager;
import org.eclipse.jetty.util.ssl.X509;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import java.security.Principal;
import java.util.Collection;

/**
 * Пользовательский SniSelector, который всегда возвращает значение для localhost
 * независимо от запрошенного имени хоста в SNI.
 */
public class CustomSniSelector implements SniX509ExtendedKeyManager.SniSelector {

    @Override
    public String sniSelect(String s, Principal[] principals, SSLSession sslSession, String s1, Collection<X509> collection) throws SSLHandshakeException {
        // Возвращаем фиксированный алиас независимо от serverName
        // Этот алиас должен соответствовать алиасу вашего сертификата в хранилище ключей
        return "superfly-server";
    }
}

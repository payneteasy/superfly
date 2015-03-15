# Installation #

Q. When trying to connect to superfly via its remote service, my system gets 302 redirect, and logs on server are clear. How can I get a clue on what's wrong?

A. First of all, make sure that there is a subsystem on Subsystems page in Superfly which name matches CN name in SSL certificate which it uses to connect to the Superfly server.
If this doesn't help, add the following line to Superfly's WEB-INF/log4j.properties file:
```
log4j.logger.org.springframework.security.web.authentication.preauth.x509=DEBUG
```
You can do it just in the unpacked application directory. Then initiate an API request (see below). If you see something like the following
```
DEBUG X509AuthenticationFilter   - No client certificate found in request.
```
then client does not send or server does not accept a certificate, or certificate is invalid. In such a case you need to carefully check server config (so it accepts certificates) and whether server trusts the certificate a client tries to send.

Q. How do I debug an API request?

A. You can run SslTestMain program. Its config is in test-ssl.xml. It's not too user-friendly way, but it's better than nothing for difficult cases.
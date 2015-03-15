# Introduction #

Using Single Sign-on (SSO) you can implement a single login to multiple systems.

# SSO mechanics #

SSO is implemented in the following way in Superfly:

  * When a Subsystem needs to authenticate a User, it redirects them to Superfly's SSO login page
  * On that page, a User is authenticated by Superfly for the Subsystem that initiated this operation (this could be password or password+OTP authentication which are supported out of the box, or anything you you'd wish to implement)
  * When a User is authenticated successfully, an SSO Session is created, its ID is saved to a cookie, and user is redirected back to an initiating Subsystem with a one-time Subsystem Token issued for that Subsystem
  * When User arrives to the Subsystem, Subsystem exchanges that token to User's roles and actions, so further user proceeds with login as usual: selects a role or authentication is just finished, if there is just one role
  * When User tries to login to other Subsystem to which he has access, that Subsystem redirects a user to SSO login page at Superfly again, and if SSO Session is still valid (it's obtained via cookie), User is not made to enter password and so on, but instead Subsystem issues a Subsystem token and user is redirected with it to the Subsystem

# SSO Session expiration #

SSO Session has a limited expiration time. In contradistintion to the normal webserver-maintained HTTP session, SSO Session is not updated automatically on user's activity, so to avoid it being constantly expiring, Subsystem has to track active sessions and **touch** SSO Sessions on a timely basis. This is the purpose of SSOService.touchSessions() method.

Besides this, SSO Session is always updated when a user is redirected via sso login page (i.e. when some Subsystem sends him to authentication and User is authenticated with that SSO Session).

# Single Sign-out #

When a User logs out from a web application, it may be desireable to destroy an SSO Session, too. This is implemented by redirecting a user to a special SSO logout page.

# SSO support in client libraries #

To add support for SSO to existing Spring application which uses Superfly for authentication, do the following:

  1. Add the following line as the first line to the `<sec:http>` element:
```
    <sec:custom-filter ref="ssoAuthenticationProcessingFilter" after="PRE_AUTH_FILTER"/>
```
  1. Add the following line to the providers list (it must be before all other providers):
```
    <ref bean="ssoAuthenticationProvider"/>
```
  1. Add SSO authentication processing filter definition:
```
    <bean id="ssoAuthenticationProcessingFilter" class="com.payneteasy.superfly.security.SuperflySSOAuthenticationProcessingFilter"
            parent="abstractAuthenticationProcessingFilter">
    </bean>
```
  1. Add SSO authentication provider definition:
```
    <bean id="ssoAuthenticationProvider" class="com.payneteasy.superfly.security.CompoundAuthenticationProvider">
        <property name="enabled" value="true"/>
        <property name="delegateProvider">
		    <bean class="com.payneteasy.superfly.security.SuperflySSOAuthenticationProvider">
		        <property name="ssoService" ref="ssoService"/>
		    </bean>
        </property>
        <property name="supportedSimpleAuthenticationClasses">
            <list>
                <value>com.payneteasy.superfly.security.authentication.SSOAuthenticationRequest</value>
            </list>
        </property>
    </bean>
```
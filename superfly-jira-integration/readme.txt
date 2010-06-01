	Superfly for Jira Integration
	=============================

	
  1. Installation
  ---------------
	
  To install this on Jira and switch Jira to use Superfly as authentication
  provider, do the following:
  
  1. Stop Jira instance
  2. Copy the following JARs to JIRA's WEB-INF/lib directory:
  	superfly-remote-api-xxx.jar
  	superfly-common-xxx.jar
  	superfly-client-xxx.jar
  	superfly-jira-integration-xxx.jar
  	httpclient-3.x.jar (if not there yet)
  	slf4j-api-xxx.jar (if not there yet)
  	slf4j-log4j12-xxx.jar (if not there yet)
  3. Modify WEB-INF/web.xml:
    3.0. Please note that if you're deploying to Jira 4.0 or higher, you don't
      need to define filters, filter mappings, context params or context
      listeners. For Jira 4.0 or higher, you only need to explicitly define the
      following listener: com.payneteasy.superfly.client.session.SessionMappingMaintainingListener
      (it's the last in section 3.3). 
  	3.1. Add the following code after all other filter definitions:
  	
<!-- Superfly:START -->
    <filter>
		<filter-name>logoutNotificationSinkFilter</filter-name>
		<filter-class>com.payneteasy.superfly.client.session.LogoutNotificationSinkFilter</filter-class>
	</filter>
    <filter>
		<filter-name>usersChangedNotificationSinkFilter</filter-name>
		<filter-class>com.payneteasy.superfly.jira.filter.UsersChangedNotificationSinkFilter</filter-class>
	</filter>
<!-- Superfly:END -->

	3.2. Add the following code after 'encoding' filter mapping:
	
<!-- Superfly:START -->
    <filter-mapping>
		<filter-name>logoutNotificationSinkFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>
    <filter-mapping>
		<filter-name>usersChangedNotificationSinkFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>
<!-- Superfly:END -->

	3.3. Add the following code anywhere where listener definitions are allowed
	(for instance, after all listeners):
	
    <!-- Superfly:START - Listeners -->
    <context-param>
        <param-name>superfly-properties-location</param-name>
        <param-value>classpath:superfly/superfly.properties</param-value>
    </context-param>
    <listener>
        <listener-class>com.payneteasy.superfly.jira.init.InitializerContextListener</listener-class>
    </listener>
    <listener>
		<listener-class>com.payneteasy.superfly.client.session.SessionMappingMaintainingListener</listener-class>
	</listener>
    <!-- Superfly:END -->

  4. Modify WEB-INF/classes/osuser.xml
    4.1. Comment out the following provider definitions (note that class names
        may be different for different Jira versions):
    
	<provider class="com.atlassian.core.ofbiz.osuser.CoreOFBizCredentialsProvider">
		<property name="exclusive-access">true</property>
	</provider>

	<provider class="com.opensymphony.user.provider.ofbiz.OFBizProfileProvider">
		<property name="exclusive-access">true</property>
	</provider>

	<provider class="com.opensymphony.user.provider.ofbiz.OFBizAccessProvider">
		<property name="exclusive-access">true</property>
	</provider>
	
	4.2. Add the following after the lines which were commented out:
	
<!-- Superfly:START -->
    <provider class="com.payneteasy.superfly.jira.provider.SuperflyCredentialsProvider"/>
    <provider class="com.payneteasy.superfly.jira.provider.SuperflyAccessProvider"/>
    <provider class="com.payneteasy.superfly.jira.provider.SuperflyProfileProvider"/>
<!-- Superfly:END -->

  5. Modify WEB-INF/classes/propertyset.xml: add the following lines:
	
    <!-- Superfly:START -->
    <propertyset name="superfly" class="com.payneteasy.superfly.jira.provider.SuperflyPropertySet"/>
    <!-- Superfly:END -->
    
  6. Modify WEB-INF/classes/seraph-config.xml:
    6.1. Comment out the following line (note that class name may be different
        for different Jira versions):
    
		<authenticator class="com.atlassian.seraph.auth.DefaultAuthenticator"/>
		
	6.2. Add the following just after the line which was commented out:
	
<!-- Superfly:START - Jira Authenticator -->
    <authenticator class="com.payneteasy.superfly.jira.auth.SuperflyJiraAuthenticator"/>
<!-- Supefly:END -->

  7. Copy 'superfly' directory from src/main/config to WEB-INF/classes
  directory and configure its contents, if needed (see the following steps).
  8. Configure WEB-INF/classes/superfly/superfly.properties:
    * superfly.sso.service.host and superfly.sso.service.secure.port must match
      host and port for your Superfly instance;
    * superfly.sso.service.url must match your SSO service URI
    * subsystem.name must match the name of a subsystem which you've chosen
      for your Jira instance on the Superfly side; it must match the CN of
      your SSL certificate (default is 'jira')
    * principal.name must match the name of a principal through which actions
      are assigned to Jira users (default is 'jira')
    * actions.list must be a comma-separated list of groups you would like to
      have in Jira (it's called 'actions.list' as Superfly's actions are mapped
      to Jira's groups).
    * configure locations of stores and their passwords (by default, a demo
      store is used in which client certificate has CN equal to 'jira'). Don't
      forget that:
      - client certificate must be trusted by Superfly server (so it must be
        imported to server's truststore or it must be signed with certificate
        imported to server's truststore)
      - Superfly server's certificate must be trusted by the truststore you
        supply here
      - CN of the client certificate must match the subsystem name you created
        for this Jira instance on Superfly server


  2. Superfly server configuration
  --------------------------------
  
  1. Create a subsystem for your Jira instance - its name must match CN of the
    client certificate that will be used by Jira to authenticate itself to the
    Superfly server.
  2. Create a role and map a principal that will be used by Jira (by default
    the principal is 'jira'). Principal name must match the principal.name value
    (see 'Installation').
  3. Launch the Jira instance with this Integration installed so that Jira
    sends its groups (which are actions for Superfly).
  4. Assign all these actions to the created role.
  5. Create users and assign actions to them; when an action is assigned to
    a user through role with Jira principal, this causes that user to have the
    group with the same name as action is named in Jira.
  
  After this, users should be able to login to Jira.
  
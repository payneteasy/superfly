Test mode is mode in which superfly-spring-security will not interact with a Superfly server but instead will work autonomously. In this mode:

  * a list of roles is predefined in superfly-client.xml configuration
  * username and password is predefined there, too
  * when a user tries to log in, all roles which are defined here are offered to them to select from
  * when a user selects any role, all actions discovered in application are given to them

Test mode is enabled in web.xml like this:

```
	<context-param>
		<param-name>superfly-local-auth</param-name>
		<param-value>true</param-value>
	</context-param>
	<context-param>
		<param-name>superfly-server-auth</param-name>
		<param-value>false</param-value>
	</context-param>
```
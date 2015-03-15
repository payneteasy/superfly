# Getting started #

## Creating a dummy Superfly-enabled application ##

  1. Checkout a superfly-demo application from superfly trunk: svn co https://superfly.googlecode.com/svn/trunk/superfly/superfly-demo
  1. Change package names in code, artifactId and groupId in pom.xml.
  1. [Configure Superfly server](ConfigureSuperflyServer.md) or [enable test mode to develop without a Superfly server](EnableTestMode.md)

## Making an existent Spring-security enabled application Superlfy-enabled ##

  1. Checkout a superfly-demo application from superfly trunk: svn co https://superfly.googlecode.com/svn/trunk/superfly/superfly-demo
  1. Add the following spring context files:
```
	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>
        	/WEB-INF/superfly-client.xml
        	/WEB-INF/spring-security-2.0.xml
                </param-value>
	</context-param>
```
  1. Add the following to your web.xml:
```
	<listener>
		<listener-class>com.payneteasy.superfly.client.session.SessionMappingMaintainingListener</listener-class>
	</listener>
	<filter>
		<filter-name>logoutNotificationSinkFilter</filter-name>
		<filter-class>com.payneteasy.superfly.client.session.LogoutNotificationSinkFilter</filter-class>
	</filter>
	<filter-mapping>
		<filter-name>logoutNotificationSinkFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>
```
  1. [Configure Superfly server](ConfigureSuperflyServer.md) or [enable test mode to develop without a Superfly server](EnableTestMode.md)

## Securing Wicket pages ##

```
@Secured("ROLE_ADMIN")
public class AdminOnlyPage extends BasePage {
...
}
```

## Configuring action sources ##

Action sources are configured in superfly-client.xml. Here's a default example:

```
	<bean id="xmlActionDescriptionCollector" class="com.payneteasy.superfly.client.XmlActionDescriptionCollector">
		<property name="resource">
			<bean class="org.springframework.core.io.ClassPathResource">
				<constructor-arg value="actions.xml"/>
			</bean>
		</property>
	</bean>
	
	<bean id="scanningActionDescriptionCollector" class="com.payneteasy.superfly.client.ScanningActionDescriptionCollector">
		<property name="basePackages">
			<list>
				<value>com.payneteasy.superfly.demo.web.wicket</value>
			</list>
		</property>
	</bean>
	
	<bean id="mergingActionDescriptionCollector" class="com.payneteasy.superfly.client.MergingActionDescriptionCollector">
		<property name="collectors">
			<list>
				<ref bean="xmlActionDescriptionCollector"/>
				<ref bean="scanningActionDescriptionCollector"/>
			</list>
		</property>
	</bean>
```

This discovers:

  * actions specified in @Secured annotations on classes which reside in com.payneteasy.superfly.demo.web.wicket and its subpackages
  * actions listed in actions.xml file on classpath
[![Build Status](https://travis-ci.org/payneteasy/superfly.svg?branch=master)](https://travis-ci.org/payneteasy/superfly)

Each web-application usually has some security: users, roles, permissions. When your organization posesses several systems, a situation may arise when the same user has to present in several systems at once. It is a burden to maintain users/roles/permissions in different systems at the same time. Superfly intends to alleviate such a burden.

  * It allows you to register users on a central server and give them permissions there.
  * It integrates seamlessly with spring-securiry.
  * It exposes its APIs so you're not tied to spring-security integration.
  * [Redirect-based mode](../wiki/SingleSignOn.md) is supported which enables user to enter password once and then be  authenticated to all the systems.
  * No-redirect mode is supported, in which user does not even know that some central server authenticates them.
  * Jira integration is provided (so you can control Jira's users permissions in the same way, from the central server).

## Project news ##

## August 19, 2018: Superfly supports Java 9 and 10 ##

Starting with release 1.7-11, the project successfully builds and runs on Java 9+ (Java 9 and 10 were checked).
Please note that Tomcat 7 will not work in Java 9+, so you'll have to upgrade Tomcat as well is you are planning to run Superfly on these recent Java versions.
Minimal Java version is raised to 8.
Spring version is upgraded to 5.

## February 8, 2013: version 1.4-1 released ##

[Single Sign-on](../wiki/SingleSignOn.md) (SSO) based on redirects is implemented. Full spring-security integration is included.

## November 13, 2012: version 1.3-8 released ##

All HttpClient timeouts are made configurable; default timeouts are configured which eliminates a freeze possibility.

## September 10, 2012: version 1.3-7 released ##

All dynamically-generated URLs are made compatible with nginx mod\_security module.

[More news...](../wiki/ProjectNews.md)

## Starting points ##

  * [Get started!](../wiki/GettingStarted.md)
  * [SecurityModel](../wiki/SecurityModel.md)
  * [Domain entities and how they interrelate](../wiki/DomainModel.md)
  * [Installing on Tomcat](../wiki/IntallOnTomcat.md)
  * [FAQ](../wiki/FAQ.md)

# Gratitudes #

Thanks to [JProfiler](http://www.ej-technologies.com/products/jprofiler/overview.html) for their wonderful [Java profiler](http://www.ej-technologies.com/products/jprofiler/overview.html).

Each web-application usually has some security: users, roles, permissions. When your organization posesses several systems, a situation may arise when the same user has to present in several systems at once. It is a burden to maintain users/roles/permissions in different systems at the same time. Superfly intends to alleviate such a burden.

  * It allows you to register users on a central server and give them permissions there.
  * It integrates seamlessly with spring-securiry.
  * It exposes its APIs so you're not tied to spring-security integration.
  * [Redirect-based mode](SingleSignOn.md) is supported which enables user to enter password once and then be  authenticated to all the systems.
  * No-redirect mode is supported, in which user does not even know that some central server authenticates them.
  * Jira integration is provided (so you can control Jira's users permissions in the same way, from the central server).

## Project news ##

## February 8, 2013: version 1.4-1 released ##

[Single Sign-on](SingleSignOn.md) (SSO) based on redirects is implemented. Full spring-security integration is included.

## November 13, 2012: version 1.3-8 released ##

All HttpClient timeouts are made configurable; default timeouts are configured which eliminates a freeze possibility.

## September 10, 2012: version 1.3-7 released ##

All dynamically-generated URLs are made compatible with nginx mod\_security module.

[More news...](ProjectNews.md)

## Starting points ##

  * [Get started!](GettingStarted.md)
  * [SecurityModel](SecurityModel.md)
  * [Domain entities and how they interrelate](DomainModel.md)
  * [Installing on Tomcat](IntallOnTomcat.md)
  * [FAQ](FAQ.md)

# Gratitudes #

Thanks to [JProfiler](http://www.ej-technologies.com/products/jprofiler/overview.html) for their wonderful profiler.
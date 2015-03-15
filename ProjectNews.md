## February 8, 2013: version 1.4-1 released ##

[Single Sign-on](SingleSignOn.md) (SSO) based on redirects is implemented. Full spring-security integration is included.

## November 13, 2012: version 1.3-8 released ##

All HttpClient timeouts are made configurable; default timeouts are configured which eliminates a freeze possibility.

## September 10, 2012: version 1.3-7 released ##

All dynamically-generated URLs are made compatible with nginx mod\_security module.

## September 04, 2012: version 1.3-5 released ##

Extended user status information is accessible via SSOService.

## January 18, 2012: version 1.3-4 released ##

Fixes in user-role-action associations editing via UI.

## January 05, 2011: version 1.3-1 released ##

Reworked mail server management: now any number of SMTP servers may be defined. Each subsystem can be assigned one of these SMTP servers.

## Novemver 16, 2010: version 1.2-4 released ##

Made it possible to edit a user and reset their password and OTP table via an SSOService.

## Novemver 02, 2010: version 1.2-2 released ##

It adds a possibility for the user to save their PGP public key, so it may be used to send something to the user (via email) in encrypted form. This is currently used to send OTP tables to a user.

## October 18, 2010: version 1.2 released ##

This version mainly strives to satisfy PCI DSS requirements so Superfly may be used as a component in a system with high security which is used to process credit cards data. Here are our new features:

  * _PCI DSS_ requirements are satisfied (see below in details)
  * _Password encryption_: all passwords are hashed. Salt may be configured to be used. Any JCE-supported hashing algorithm may be used (SHA-256 is preconfigured for high security policy).
  * _Password expiration_ makes users to change their passwords with time. For PCI DSS, max password age is configured to be 90 days.
  * _Accounts suspension_ feature suspends accounts which were not used for some period in time (for PCI DSS, period is 90 days).
  * _Password form restrictions_: too weak, too short, or used passwords are not allowed for strict mode
  * _First-time passwords_ are used for the first login and after password reset. They only allow user to change their password.
  * _Two-factor authentication_ using OTP (one-time password) technique is implemented.
  * _OTP extension point_ is defined, so you may implement your own OTP provider.
  * _Logging_: every operation on system objects, as well as authentication attempts, are logged.

Most of the above is not needed for a 'usual', non-paranoidly-secure case, so you can easily configure what policy you need: 'none' (default one, with almost no PCI DSS conformance, but with least annoyance) or 'pcidss' (PCS DSS confirmant).

## October 03, 2010: version 1.1-2 released ##

1.1-2 contains minor changes to UI. In particular, actions are sorted in the lists.

## August 09, 2010: version 1.1 released ##

1.1 version adds several important features:

  * _Local mode_ to ease development
  * _Spring 3 support_
  * _Jira integration_ allows to use a Superfly server as a security provider for Jira
  * _UI inhancements_ make UI more friendly
  * This is the first release which is available from a Maven Central Repo

## July, 17, 2010: Will be in Maven Central Repo soon! ##

Thanks to Sonatype, our next release (and all subsequent releases) will be available in Maven Central Repository.

## April 07, 2010: Superfly on Google Code ##

Superfly project which goal is to build a centralized web authentication system for Java has moved to Google Code. Source code is licensed under Apache 2 Licence.
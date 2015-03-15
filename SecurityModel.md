# Introduction #

Superfly security model is based on a standard JEE security model.

  * There's a user who needs to log in to a system (i.e. authenticate itself)
  * User has some number of permissions (in JEE they're called roles, in Superfly they are actions)
  * These permissions are checked by any standard ways (like Spring Security's SecurityFilter)
  * There's an extension: along with fine-grained permissions user is also assigned some security class which is called **principal** in Superfly. This one can be used for coarser-grained security desicions (for instance, in DB procedures).

# User log in process #

When a user logs in to a subsystem, he's first asked to authenticate himself. This may be done via a username-password pair.

After being authenticated, a user is asked to select a role with which he'd like to work in the system. User can only have one role when he's logged in.

When a user has selected, a role he gets from it the following:

  * Principal (currently, it's defined as an attribute of a role)
  * Number of actions

# How actions are obtained from a role #

There're two ways in which an action may be granted through a role:

  * If an action is assigned to a group which is in turn assigned to a role, user gets such an action unconditionally (these are **unconditional actions**)
  * If an action is assigned to a role directly, user gets such an action only if it's confirmed for the given user (these are **confirmable actions**)

# Why confirmable actions? #

Most of actions are meant to be given unconditionnally (i.e. via groups). But sometimes you want to give some ''superuser'' some permission, and you don't want to give it to other users of the same role. You could create a clone of a role and give it this super-action via group, but it's pretty cumbersome desicion.

Here's when confirmable action comes into play. You assign an action directly to a role, so it becomes confirmable. Then you confirm it only for your 'superuser' but not for other users with the same role. Voila!

# Alternative security services #

Main Superfly's role is to implement standard JEE security scenario 'user logs in, gets permissions and they are checked then'. But Superfly also may serve as a user/group provider for other systems which require external user/group management. Example of such a system is Jira for which Superfly has integration.

For such systems, it's also needed to have a possibility to obtain a list of all users who is known to a system. For this reason, there's a flag in a Subsystem entity which allows a subsystem to list its users.

# Single Sign-on #

Superfly supports a Single Sign-on mode in which a user is redirected to Superfly server to authenticate and after this their're redirected back to the Subsystem. More details on the [Single Sign-on](SingleSignOn.md) page.
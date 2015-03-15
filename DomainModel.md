There're the following domain objects:

  * Subsystem
  * User
  * Principal
  * Role
  * Group
  * Action

# Subsystem #

**Subsystem** is any system which wishes to use Superfly service for authentication and authorization. For this to work, a subsystem must be registered in the Superfly system via its user interface.

After registration, subsystem may just send its identifier or use SSL with mutual authentication to authenticate itself to a Superfly instance. The latter case is recommented. For it, a subsystem identifier will be obtained from a subsystem SSL certificate.

Subsystem serves as a namespace for any other domain objects except users. This means that, for example, there may be a role called 'admin' for subsystem1 and another role called 'admin' for subsystem2.

# User #

**User** corresponds to a user in JEE vocabulary. User has username and password which they can use to authenticate itself. User also has some roles assigned to him/her in subsystems (and, possibly, comfirmed actions for ''confirmable actions'').

# Principal #

**Principal** is a logical name. When a user logs in to a subsystem and selects a role under which they will work, they get principal from a selected role. This principal then may be used to authorize actions for which there needs to be a coarse-grained access control (like in database procedures).

Currently, principal does not have a corresponding entity in Superfly code, it is defined by an attribute of a ''Role''.

# Action #

**Action** corresponds to some fine-grained permission which user may have or not have being logged in. For example, 'create new project' may be such an action. In JEE vocabulary, role corresponds to Superfly's action.

Every action belongs to a single subsystem. Actions are not created/deleted through the UI (because there may be a plenty of actions). Subsystems send their action lists using remote calls to a Superfly instance.

# Group #

**Group** is used as a container for actions. Please note that group not just groups actions, it has its own special semantics: any action which is given to a user through a group is given to them unconditionally.

Every group belongs to a single subsystem. It can only contain actions belonging to the same subsystem.

# Role #

**Role** defines 'what a user can do'. It contains two things:
  * Principal which user will have
  * Actions which user will have

Role contains groups, and it may have actions directly assigned to it. So, there're two kinds of actions in relation to roles:
  * Actions which are assigned to groups which are in turn assigned to a Role. Such actions are granted unconditionally to a user when they log in with this role.
  * Actions which are assigned to role directly. Such actions are only granted to a user when they are ''confirmed'' for this user personally.

Every role belongs to a single subsystem. It can only contain groups and actions belonging to the same subsystem.
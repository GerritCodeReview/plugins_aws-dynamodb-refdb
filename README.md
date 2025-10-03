# DEPRECATION NOTICE

GerritForge has decided to [change the license to BSL](https://gitenterprise.me/2025/09/30/re-licensing-gerritforge-plugins-welcome-to-gerrit-enterprise/)
therefore the Apache 2.0 version of this plugin is deprecated.
The recommended version of the aws-dynamodb-refdb plugin is on [GitHub](https://github.com/GerritForge/aws-dynamodb-refdb)
and the development continues on [GerritHub.io](https://review.gerrithub.io/admin/repos/GerritForge/aws-dynamodb-refdb,general).

# Gerrit DynamoDB ref-db (DEPRECATED)

This plugin provides an implementation of the Gerrit global ref-db backed by
[AWS DynamoDB](https://aws.amazon.com/dynamodb/).

Requirements for using this plugin are:

- Gerrit v3.3 or later
- DynamoDB provisioned in AWS

## Typical use-case

The global ref-db is a typical use-case of a Gerrit multi-master scenario
in a multi-site setup. Refer to the
[Gerrit multi-site plugin](https://gerrit.googlesource.com/plugins/multi-site/+/master/DESIGN.md)
for more details on the high level architecture.

# Gradle Incremental build with TestContainers + Liquibase + JOOQ Codegen

This repo demonstrates how to make a gradle incremental build that use code
generation using JOOQ codegen on a database running in a TestContainer started
on a random port, whose schema is maintained by Liquibase.

### How

The database is started using a BuildService that is only started once a task
depending on it is actually run.

A single `configureTestContainers` task depends on that BuildService.

This task has all the liquibase changelog files as input, and no output,
therefore gradle considers that task to be `UP-TO-DATE` if the liquibase
changelog hasn't changed since the last invocation.

All other tasks related to code generation:

* Liquibase `update`
* JOOQ `jooqCodegen*`

Are conditionally skipped on the condition that task `configureTestContainers`
is `UP-TO-DATE`.

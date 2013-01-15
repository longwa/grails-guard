Grails Guard
=======================
This plugin provides a convenient way to run integration tests without having to suffer repetitive startup costs. Each time an
integration test suite runs, the grails environment must be compiled and bootstrapped. For a large application, especially one with many domain objects, this
can take over a minute just to run a single testcase. Over the course of several months this can lead to days of lost productivity just waiting
for tests to run. 

This plugin borrows from the similarly named Rails plugin (although it is more targeted at just running tests in this incarnation).

The plugin essentially runs the tests in a loop, detecting changes to the application (and tests), recompiling, and re-running the tests.

Usage
-----
To run your tests in guard mode, you need to do two things:

1. Ensure that the spring reloading agent is active for test-app *(it is NOT by default for test-app, only run-app)*.
2. Run your testing in guard mode by replacing "integration:" with "guard:" when running test-app.

The minimum required is this:

    grails -reloading test-app guard: <testpatterns...>

If you want to see the console output, use:

    grails -reloading -echoOut test-app guard: <testpatterns...>

Finally, to make life easier, just create an alias in your .bashrc:

    alias guard='grails -reloading -echoOut test-app guard: '

Then run:

    guard my.package.*
    guard MyService
    guard MyService MyOtherService

This will first run the integration tests given just like the "integration:" phase would. Then it will enter
a mode where it detects changes to any artefacts and testcases. When a change is detected, it will indicate which artefact was changed
and then run the same tests again with the updates.

Configuration
-------------
* The scan frequency is every 3 seconds but can be configured via the "rerun.frequency" property.

IntelliJ Support
--------------
IntelliJ (and possibly Eclipse though I haven't tried it) can also be used to run guard mode.

In IntelliJ, run an integration test once normally. This will give you a "configuration" for the test case which can be edited.
Click to edit the profile and change "integration:" to "guard:" and add the "-reloading" flag to the end of the command line
(ensure no other -* flags are prior such as -echoOut).

When you run that configuration, IntelliJ will process the tests normally and then "spin" as if it's waiting for more tests. As you modify files,
you will see the same tests run again. Since guard never notifies the listeners that the test run is complete, IntelliJ will continue waiting for
tests until you stop it manually.

Known Issues
------------
* Since the environment is not reloaded, tests must be careful to cleanup after themselves, even if running against the in-memory database since the create-drop
only happens when the environment is bootstrapped.
* GRAILS-8026 causes the -reloading flag to be a bit picky. When running from the command line, ensure that it is the first option specified, otherwise it may be ignored


Grails Spork
=======================
This plugin provides a convenient way to run integration tests without having to suffer repetitive startup costs. Each time an
integration test suite runs, the grails environment must be compiled and bootstrapped. For an application with several hundred domain objects, this
can take over a minute just to run a single testcase. Over the course of several months this can lead to days of lost productivity just waiting
for tests to run. 

This plugin borrows and combines ideas from the similarly named Rails plugin as well as the Rails guard plugin to provide a combination of both.

The plugin consists of two parts:

1. A simple controller and service that can load and run an integration test within a running server instance.
2. A custom grails script that allows you to run a test from the command line or loop and run any modified tests automatically.

Usage
-----
Start your server using "grails run-app" as normal. If you want to run integration tests in the "test" environment (which is typical), you'll want 
to start a server with "grails test run-app" instead.

Once the server is running, you can run tests using the "spork" command:

    grails spork <test-pattern> 

The "test-pattern" can be any valid test pattern that "grails test-app" can use (see test-app for details).

Optionally, if you run "grails spork" with no arguments, it will enter "guard mode". In this mode, it will scan for changes to integration
tests every 3 seconds and automatically compile and run any tests when they change.

In both cases, the spork command will compile the tests (if needed) and then invoke the test *inside* the running instance.
---
Optionally, since the tests are executed by invoking the spork TestRunner controller, you can also run the tests manually by pointing at:

    http://localhost:8080/spork/testRunner/run?testPattern=<TestPattern>

If no pattern is given, all integration tests are run. Once the tests complete, the TestRunner will render a GSP with basic test results. You can also see the 
results and log output in the server logs or console.

Known Issues
------------
* Since the environment is not reloaded, tests must be careful to cleanup after themselves, even if running against the in-memory database since the create-drop
only happens when the server instance is restarted.

Future
----
Add a new test phase instead of using a custom script. This will allow the tests to run normally from within an editor like IntelliJ.

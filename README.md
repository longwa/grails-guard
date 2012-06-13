Grails Spork
=======================
This plugin provides a convenient way to run integration tests without having to suffer repetitive startup costs. Each time an
integration test suite runs, the grails environment must be compiled and bootstrapped. For an application with several hundred domain objects, this
can take over a minute just to run a single testcase. Over the course of several months this can lead to days of lost productivity just waiting
for tests to run. 

This plugin borrows and combines ideas from the similarly named Rails plugin as well as the Rails guard plugin to provide a combination of both.

The plugin has two components:

1. A simple controller and service that can load and run an integration test within a running server instance.
2. Custom grails scripts that allows you to run a test from the command line or loop and run any modified tests automatically.

There are two ways of using the spork plugin:

1. Normal Mode - Running test-app via command line or an IDE
2. Guard Mode - Listening for changes to test cases and running automatically

Both modes take care of recompiling the tests prior to execution. This, combined with the reloading agent of the running server instance, allows most
changes to be tested without needing to restart.

Normal Testing
--------------
Start your server using "grails run-app" as normal. If you want to run integration tests in the "test" environment (which is typical), you'll want 
to start a server with "grails test run-app" instead.

Once the server is running, you can run integration tests using the custom "spork" phase and type:

    grails test-app spork: <test-pattern>

The "spork" type is essentially the same as "integration" except instead of running the tests in place, it executes the tests via the running
application instance (currently only localhost:8080 is supported)

Guard Mode
----------
Start this mode by running the "spork" command:

    grails spork

In this mode, it will scan for changes to integration tests every 3 seconds, by default, and automatically compile and run tests as they change.
The scan frequency can be changed by setting the "recompile.frequency" property (in seconds).

Web Interface
-------------
Since the tests are executed by invoking the Spork controller, you can also run the tests manually by pointing at:

    http://localhost:8080/myApp/spork/run?testPattern=<TestPattern>

If no pattern is given, all integration tests are run. Once the tests complete, the SporkController will render a simple GSP with basic test results.
You can also see the results and output in the server logs and/or console.

Known Issues
------------
* Since the environment is not reloaded, tests must be careful to cleanup after themselves, even if running against the in-memory database since the create-drop
only happens when the server instance is restarted.
* The spork test type runs by default when you run "grails test-app". This will actually try to run the integration tests twice.

Future
----
Add a new test phase instead of using a custom script. This will allow the tests to run normally from within an editor like IntelliJ.

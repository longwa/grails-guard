Grails Guard
=======================
Guard provides a convenient way to run integration tests without having to suffer repetitive startup costs. Each time an
integration test suite runs, the grails environment must be compiled and bootstrapped. For a large application, especially one with many domain objects, this
can take over a minute just to run a single testcase. Over the course of several months this can lead to days of lost productivity just waiting
for tests to run. 

This plugin borrows from the similarly named Rails plugin (although it is more targeted at just running tests in this incarnation).

The plugin essentially runs the tests in a loop, detecting changes to both the application code and the test code. If a change is detected, 
the selected test(s) are re-run. 

Versions
--------
Due to major changes to the test infrastructure introduced in Grails 2.3 and 2.4, different versions of Guard are required depending on 
the version of Grails you are using:

* Version 1.x.x - Compatible with Grails 2.0, 2.1, or 2.2
* Version 2.0.x - Compatible with Grails 2.3
* Version 2.1.x - Compatible with Grails 2.4 

Unfortunately, it's just not possible to build one version of Guard that works reliably with all of these different versions.

Usage (Version 2.x) 
-----------------      
Starting with version 2, Guard is no longer implemented as a test phase. Guard is now enabled via the command line flag "-guard". 

There are a few advantages to this approach. From an implementation standpoint, this allows Guard to fit more seamlessly into the new 2.3 test
framework. It will also (eventually) allow Guard to support functional tests in addition to integration tests.

To run your tests in guard mode, you need to do a few things:

1. Enable the spring reloading agent by adding the "-reloading" flag to the command line
2. Disable forked mode execution for 'test' in BuildConfig.groovy 
3. Enable guard by adding the "-guard" flag to the command line when running integration test(s)

For example:

    grails -reloading -guard test-app integration: <testpatterns...>
    grails -reloading -guard test-app integration: MyServiceSpec

If you want to see the console output, use:

    grails -reloading -echoOut -guard test-app integration: <testpatterns...>

Finally, to make life easier, just create an alias in your .bashrc:

    alias guard='grails -reloading -echoOut -guard test-app integration:'

Then run:

    guard my.package.*
    guard MyService
    guard MyService MyOtherService

This will run the tests once as it normally would. Regardless of the success or failure of the tests, it will then enter 
a mode where it detects changes to application code and test code.  When a change is detected, it will indicate which file was changed
and then run the same test pattern again with the updates.

Usage (Version 1.x)
-----------------
For version 1, Guard is still implemented as a test phase replacement for "integration:".

To run your tests in guard mode, you need to do a few things:

1. Enable the spring reloading agent by adding the "-reloading" flag to the command line
2. Enable guard by specifying the "guard:" test phase after 'test-app'

For example:

    grails -reloading test-app guard: <testpatterns...>
    grails -reloading test-app guard: MyService

Finally, to make life easier, just create an alias in your .bashrc:

    alias guard='grails -reloading -echoOut test-app guard:'

Then run:

    guard my.package.*
    guard MyService
    guard MyService MyOtherService

IDE Support
-----------
IntelliJ (and possibly Eclipse though I haven't tried it) can also be used to run guard mode.

In IntelliJ, run an integration test once normally. This will give you a "configuration" for the test case which can be edited.
Click to edit the profile and add the -guard flag. You may also need to add the "-reloading" flag to the end of the command line.

When you run that configuration, IntelliJ will process the tests normally and then "spin" as if it's waiting for more tests. As you modify files,
you will see the same tests run again. Since guard never notifies the listeners that the test run is complete, IntelliJ will continue waiting for
tests until you stop it manually.

Known Issues
------------
* Since the environment is not reloaded, tests must be careful to cleanup after themselves, even if running against the in-memory database since the create-drop
only happens when the environment is bootstrapped.
* Depending on the classes being changed, you may encounter PermGem issues if Guard loops for an extending number of iterations.
* If you have a large domain model, it can be very beneficial to disable Hibernate sessionFactory reloading by setting the hibernate.reload = false flag in DataSource.groovy

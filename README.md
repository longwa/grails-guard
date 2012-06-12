Grails Spork
=======================
This plugin provides a convenient way to run integration tests without having to suffer repetitive startup costs. Each time an
integration test suite runs, the grails environment must be compiled and bootstrapped. For an application with several hundred domain objects, this
can take over a minute just to run a single testcase. Over the course of several months this can lead to days of lost productivity just waiting
for tests to run. 

This plugin borrows and combines ideas from the similarly named Rails plugin as well as the Rails guard plugin to provide a combination of both.

Usage
-----
TODO

Known Issues
------
None

Future
----
Add a new test phase instead of using a custom script. This will allow the tests to run normally from within an editor like IntelliJ.

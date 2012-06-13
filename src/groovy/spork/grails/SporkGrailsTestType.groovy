package spork.grails

import org.codehaus.groovy.grails.test.GrailsTestTypeResult
import org.codehaus.groovy.grails.test.event.GrailsTestEventPublisher
import org.codehaus.groovy.grails.test.junit4.JUnit4GrailsTestType
import org.codehaus.groovy.grails.test.support.GrailsTestMode
import org.junit.runner.Description
import org.junit.runner.Result
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

/**
 * Custom test type for running spork tests via the normal grails mechanism
 */
class SporkGrailsTestType extends JUnit4GrailsTestType {
    SporkGrailsTestType(String name, String sourceDirectory, GrailsTestMode mode) {
        super(name, sourceDirectory, mode)
    }

    @Override
    protected GrailsTestTypeResult doRun(GrailsTestEventPublisher eventPublisher) {
        // Use the default grails notifier
        def notifier = createNotifier(eventPublisher)

        // Create a new result
        def result = new Result()
        def runListener = new SporkRunListener()

        // Add a listener to this notifier
        notifier.addListener(runListener)

        suite.run(notifier)

        notifier.fireTestRunFinished(result)

        new SporkGrailsTestTypeResultAdapter(runListener)
    }
}

/**
 * Customer wrapper for returning test results
 */
class SporkGrailsTestTypeResultAdapter implements GrailsTestTypeResult {
    def listener

    SporkGrailsTestTypeResultAdapter(def listener) {
        this.listener = listener
    }

    @Override
    int getPassCount() {
        listener.finished.size() - failCount
    }

    @Override
    int getFailCount() {
        listener.failures.size()
    }
}

/**
 * Customer listener to record individual test success and failures
 */
class SporkRunListener extends RunListener {
    List<Failure> failures = []
    List<Description> finished = []

    @Override
    void testFailure(Failure failure) {
        super.testFailure(failure)
        failures << failure
    }

    @Override
    void testFinished(Description description) {
        super.testFinished(description)
        finished << description
    }
}

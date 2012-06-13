package spork.grails

import groovyx.net.http.HTTPBuilder
import org.codehaus.groovy.grails.test.GrailsTestTypeResult
import org.codehaus.groovy.grails.test.event.GrailsTestEventPublisher
import org.codehaus.groovy.grails.test.event.GrailsTestRunNotifier
import org.codehaus.groovy.grails.test.junit4.JUnit4GrailsTestType
import org.junit.runner.Description
import org.junit.runner.Result
import org.junit.runner.notification.Failure

/**
 * Test type for running tests via the spork client
 */
class SporkClientTestType extends JUnit4GrailsTestType {
    String appName

    SporkClientTestType(String appName, String name, String relativeSourcePath) {
        super(name, relativeSourcePath)
        this.appName = appName
    }

    @Override
    protected GrailsTestTypeResult doRun(GrailsTestEventPublisher eventPublisher) {
        GrailsTestRunNotifier notifier = createNotifier(eventPublisher)
        def result = new Result()
        notifier.addListener(result.createListener())

        // Execute via the spork test runner
        def http = new HTTPBuilder("http://localhost:8080/${appName}/spork/")
        def jsonResults = null
        http.get( path: 'run', query: [format: 'json', testPattern: testTargetPatterns.collect{ it.rawPattern } ] ) { resp, json ->
            json.each {
                // JSONNull object has some weird == behavior, workaround calling .equals()
                if( it && !it.equals(null) ) {
                    jsonResults = it
                }
            }
        }

        def resultAdapter = null
        if( jsonResults ) {
            jsonResults.listener.finished.each {
                notifier.fireTestStarted(toDescription(it))
            }
            jsonResults.listener.failures.each {
                notifier.fireTestFailure(toFailure(it))
            }
            jsonResults.listener.finished.each {
                notifier.fireTestFinished(toDescription(it))
            }

            resultAdapter = new SporkGrailsTestTypeResultAdapter(jsonResults.listener)
        }

        notifier.fireTestRunFinished(result)
        resultAdapter
    }

    /**
     * To satisfy listeners, we need to deserialize into the actual JUnit classes
     *
     * @param jsonFailure
     * @return
     */
    protected Failure toFailure(def jsonFailure) {
        new Failure(toDescription(jsonFailure.description), toThrowable(jsonFailure))
    }

    protected Description toDescription(def jsonDescription) {
        Description.createTestDescription(testClassLoader.loadClass(jsonDescription.className), jsonDescription.methodName)
    }

    /**
     * Try to reconstruct the exception from the JSON object
     *
     */
    protected Throwable toThrowable(def jsonFailure) {
        // Try to create the same exception on this side
        Throwable t = null
        try {
            int idx = jsonFailure.trace.indexOf(":")
            String errorType = jsonFailure.trace.substring(0, idx)
            if( idx > 0 ) {
                t = Class.forName(errorType).newInstance(jsonFailure.exception.message) as Throwable
            }
        }
        // If we can't, just create a generic exception
        catch( ignored ) {
        }

        if( !t ) {
            t = new ExceptionWrapper(jsonFailure.exception.message)
        }

        // Populate the correct stack trace
        StackTraceElement[] ste = jsonFailure.exception?.stackTrace?.collect {
            new StackTraceElement(it.className, it.methodName, it.fileName, it.lineNumber)
        } as StackTraceElement[]

        if( ste ) {
            t.setStackTrace(ste)
        }

        return t
    }
}

class ExceptionWrapper extends Throwable {
    ExceptionWrapper(String s) {
        super(s)
    }

    @Override
    String toString() {
        message
    }
}

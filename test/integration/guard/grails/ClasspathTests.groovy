package guard.grails

import org.junit.After
import org.junit.Before

class ClasspathTests extends GroovyTestCase {

    def sampleService

    @Before
    void setUp() {
        println "Running setUp()"
    }

    @After
    void tearDown() {
        println "Running tearDown()"

    }

    void testSuccess() {
        println "Running testSuccess()"
        assert true
    }

    void testFailure() {
        println "Running testFailure()"

    }

    void testAssertionFailure() {
        def something = "Something"
        assert something != null

    }

    void testThrowException() {
        println sampleService.helloWorld()
    }
}

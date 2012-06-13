package spork.grails

import org.junit.After
import org.junit.Before

class ClasspathTests extends GroovyTestCase {

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
        fail "This method is designed to fail"
    }

    void testAssertionFailure() {
        def something = "Something"
        assert something == null
    }

    void testThrowException() {
        throw new IllegalArgumentException("You can't do whatever you tried to do here")
    }
}

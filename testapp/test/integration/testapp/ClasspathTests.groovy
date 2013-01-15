package testapp

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

    void testServiceCall() {
        assert sampleService.helloWorld()
    }
}

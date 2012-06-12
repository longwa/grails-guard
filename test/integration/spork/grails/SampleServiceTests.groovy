package spork.grails

class SampleServiceTests extends GroovyTestCase {
    def sampleService

    void testServiceIsInjected() {
        assert sampleService != null
    }

    void testHelloWorld() {
        assert sampleService.helloWorld() == "Hello World!"
    }
}

package guard.grails

/**
 * This is for testing within the plug. I found some weird behavior at one point
 * with testing when using an in-place plugin. This allows me to test
 * with the testapp and the guard context.
 */
class ReloadServiceTests extends GroovyTestCase {
    def sampleService

    void testServiceIsInjected() {
        assert sampleService != null
    }

    void testHelloWorld() {
        assert sampleService.helloWorld() == "Hello World!"
    }
}

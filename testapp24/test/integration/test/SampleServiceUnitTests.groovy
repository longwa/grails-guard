package test

import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin

@TestMixin(IntegrationTestMixin)
class SampleServiceUnitTests   {
    def sampleService

    void testServiceIsInjected() {
        assert sampleService != null
    }
    void testHelloWorld() {
        assert sampleService.helloWorld() == "Hello World!"
    }
}

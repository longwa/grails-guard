package test

import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin

@TestMixin(IntegrationTestMixin)
class ClasspathTests {
    def sampleService

    void setUp() {
        println "Running setUp()"
    }

    void tearDown() {
        println "Running tearDown()"
    }

    void testServiceCall() {
        assert sampleService.helloWorld()
    }
}

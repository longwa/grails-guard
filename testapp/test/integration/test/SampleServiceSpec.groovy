package test

import grails.test.spock.IntegrationSpec

class SampleServiceSpec extends IntegrationSpec {
    def sampleService

    def "injected sampleService is present"() {
        when:
        def msg = sampleService.helloWorld()

        then:
        msg == "Hello World!"
    }
}
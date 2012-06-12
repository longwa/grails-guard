package spork.grails

/**
 * Sample service for testing service injection
 */
class SampleService {
    static transactional = false

    def helloWorld() {
        "Hello World!"
    }
}

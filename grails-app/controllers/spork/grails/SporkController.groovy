package spork.grails

import grails.converters.JSON

class SporkController {
    def testRunnerService

    /**
     * Run the given test using the grails test patterns accepted by test-app
     *
     * @param testPattern ex. grails.spork.*, Classpath, *
     */
    def run() {
        def testPattern = params.testPattern
        if( testPattern instanceof String ) {
            testPattern = [testPattern]
        }

        // Default to all tests
        if ( !testPattern ) {
            testPattern = ["**.*"]
        }

        // Run the tests
        SporkGrailsTestTypeResultAdapter results = testRunnerService.runTests(testPattern)

        int passed = results?.passCount ?: 0
        int failed = results?.failCount ?: 0

        if( params.format == "json" ) {
            render([results] as JSON)
        }
        else {
            [testPattern: testPattern.toString(), failures: results?.listener?.failures, finished: results?.listener?.finished, totalPassed: passed, totalFailed: failed]
        }
    }
}

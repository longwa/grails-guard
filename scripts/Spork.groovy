import org.codehaus.groovy.grails.test.junit4.JUnit4GrailsTestType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

includeTargets << grailsScript("_GrailsClean")
includeTargets << grailsScript("_GrailsTest")

recompileFrequence = Integer.parseInt(System.getProperty("recompile.frequency") ?: "3")

// Base
serverHost = System.getProperty("server.host") ?: "localhost"
serverPort = System.getProperty("server.port") ?: "8080"

target(main: "Run integration test(s) inside a running server") {
    def tests

    // Re-run failed tests
    reRunTests = argsMap["rerun"]
    if( reRunTests ) {
        tests = getFailedTests()
    }
    else {
        tests = argsMap.params
    }

    // If we are given tests to run, run them and exit. Otherwise, just loop and run in guard mode
    if( tests ) {
        compile()
        runTests(tests)
    }
    else {
        watchForTestChanges()
    }
}

def watchForTestChanges() {
    grailsConsole.updateStatus "Checking for test changes every ${recompileFrequence} seconds..."

    //noinspection GroovyInfiniteLoopStatement
    while(true) {
        sleep(recompileFrequence * 1000)
        compile()
    }
}

/**
 * Compile integration tests
 */
def compile() {
    def testType = new JUnit4GrailsTestType("integration", "integration")
    def relativePathToSource = testType.relativeSourcePath
    if (relativePathToSource) {
        def source = new File("${testSourceDir}", relativePathToSource)

        //noinspection GroovyAssignabilityCheck
        def dest = new File(grailsSettings.testClassesDir, relativePathToSource)

        // Compile the tests
        compileTests(testType, source, dest)
    }
}

def runTests(tests) {
    long startTime = new Date().time

    def http = new HTTPBuilder( "http://${serverHost}:${serverPort}/spork/testRunner/",  )
    http.get( path: 'run', query: [format: 'json', testPattern: tests] ) { resp, json ->
        json.each {
            outputResults(it, new Date().time - startTime)
        }
    }
}

def outputResults(def results, def executionTime = 0) {
    results.listener.failures.each { failure ->
        grailsConsole.addStatus "Failure: "
        grailsConsole.log "${failure.description.displayName}"
        grailsConsole.log failure.trace
        grailsConsole.log ""
    }

    grailsConsole.addStatus "Completed ${results.listener.finished.size()} integration tests, ${results.failCount} failed in ${executionTime}ms"
    if( results.failCount > 0 ) {
        grailsConsole.error "Tests FAILED"
    }
}

def getFailedTests() {
    File file = new File("${testReportsDir}/TESTS-TestSuites.xml")
    if (!file.exists()) {
        return []
    }

    def xmlParser = new XmlParser().parse(file)
    def failedTests = xmlParser.testsuite.findAll { it.'@failures' =~ /.*[1-9].*/ || it.'@errors' =~ /.*[1-9].*/}

    return failedTests.collect {
        String testName = it.'@name'
        testName = testName.replace('Tests', '')
        def pkg = it.'@package'
        if (pkg) {
            testName = pkg + '.' + testName
        }
        return testName
    }
}

setDefaultTarget(main)

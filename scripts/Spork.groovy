import groovyx.net.http.HTTPBuilder
import org.codehaus.groovy.grails.test.junit4.JUnit4GrailsTestType

includeTargets << grailsScript("_GrailsClean")
includeTargets << grailsScript("_GrailsTest")

recompileFrequence = Integer.parseInt(System.getProperty("recompile.frequency") ?: "3")

// Base
serverHost = System.getProperty("server.host") ?: "localhost"
serverPort = System.getProperty("server.port") ?: "8080"

target(main: "Run integration test(s) inside a running server") {
    // If we are given tests to run, run them and exit. Otherwise, just loop and run in guard mode
    def tests = argsMap.params
    if( tests ) {
        compile()
        runTests(tests)
    }
    else {
        watchForTestChanges()
    }
}

def watchForTestChanges() {
    grailsConsole.addStatus "Checking for test changes every ${recompileFrequence} seconds..."
    long lastScanned = 0

    //noinspection GroovyInfiniteLoopStatement
    while(true) {
        def modifiedFiles = []
        if( lastScanned ) {
            modifiedFiles = findUpdatedTests(lastScanned)
        }

        // If anything was modified, compile and run
        if( modifiedFiles ) {
            modifiedFiles.each { grailsConsole.log "Executing updated test: ${it}" }
            compile()
            runTests(modifiedFiles)
        }

        lastScanned = new Date().time
        sleep(recompileFrequence * 1000)
    }
}

def findUpdatedTests(long lastScanned) {
    def files = []
    def testDir = new File("integration", testSourceDir)
    testDir.traverse {
        if( it.isFile() && it.lastModified() > lastScanned ) { files << it }
    }
    files.collect { File f ->
        f.absolutePath - testDir.absolutePath
    }
}

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
    if( tests ) {
        long startTime = new Date().time
        def http = new HTTPBuilder("http://${serverHost}:${serverPort}/spork/testRunner/")
        http.get( path: 'run', query: [format: 'json', testPattern: tests] ) { resp, json ->
            json.each {
                outputResults(it, new Date().time - startTime)
            }
        }
    }
}

def outputResults(def results, def executionTime = 0) {
    if( !results?.equals(null) ) {
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
        else {
            grailsConsole.info "Tests PASSED"
        }
    }
    else {
        grailsConsole.addStatus "Completed 0 tests. Maybe the test pattern did not match any tests?"
    }
}

setDefaultTarget(main)

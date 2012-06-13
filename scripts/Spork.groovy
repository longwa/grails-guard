import org.codehaus.groovy.grails.test.GrailsTestTargetPattern

includeTargets << grailsScript("_GrailsTest")

recompileFrequence = Integer.parseInt(System.getProperty("recompile.frequency") ?: "3")

target(main: "Run integration test(s) inside a running server") {
    watchForTestChanges()
}

def watchForTestChanges() {
    grailsConsole.addStatus "Checking for test changes every ${recompileFrequence} seconds..."
    long lastScanned = 0

    //noinspection GroovyInfiniteLoopStatement
    while(true) {
        List<String> modifiedFiles = []
        if( lastScanned ) {
            modifiedFiles = findUpdatedTests(lastScanned)
        }

        // If anything was modified, compile and run
        if( modifiedFiles ) {
            modifiedFiles.each { grailsConsole.log "Executing updated test: ${it}" }

            // Set the target patterns
            testTargetPatterns = modifiedFiles.collect { new GrailsTestTargetPattern(it) } as GrailsTestTargetPattern[]

            // Run the tests
            def sporkTestType = loadSporkTestTypeClass().newInstance("spork", "integration")
            currentTestPhaseName = "spork"

            processTests(sporkTestType)

            currentTestPhaseName = null
        }

        lastScanned = new Date().time
        sleep(recompileFrequence * 1000)
    }
}

def findUpdatedTests(long lastScanned) {
    def files = []

    //noinspection GroovyAssignabilityCheck
    def testDir = new File("integration", testSourceDir)
    testDir.traverse {
        if( it.isFile() && it.lastModified() > lastScanned ) { files << it }
    }
    files.collect { File f ->
        f.absolutePath - testDir.absolutePath
    }
}

loadSporkTestTypeClass = {->
    def doLoad = {-> classLoader.loadClass('spork.grails.SporkClientTestType') }
    try {
        doLoad()
    }
    catch(ClassNotFoundException ignored) {
        includeTargets << grailsScript("_GrailsCompile")
        compile()
        doLoad()
    }
}

setDefaultTarget(main)

// Load the custom test type
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

// Load spork test types
sporkTests = []

eventAllTestsStart = {
    phasesToRun << "spork"
    sporkTests << loadSporkTestTypeClass().newInstance(metadata.'app.name', "spork", "integration")
}

sporkTestPhasePreparation = {
    println metadata.'app.name'
}

sporkTestPhaseCleanUp = {
}

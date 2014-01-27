//noinspection GroovyUnusedAssignment
eventAllTestsStart = {
    // Guard can either run as a phase using guard: <pattern> or via a flag -guard
    if( targetPhasesAndTypes['guard'] || projectTestRunner.testOptions['guard'] ) {

        // Support specifying guard as a test phase. If given as a phase, remove the
        // guard phase name and replace with the same types for integration.
        if (targetPhasesAndTypes['guard']) {
            def types = targetPhasesAndTypes.remove('guard')
            targetPhasesAndTypes['integration'] = types
        }

        // Replace the integration configurer with one that loops instead of cleaning up
        def configurer = loadGuardTestPhaseConfigurer().newInstance([projectTestRunner, projectCompiler, projectLoader] as Object[])
        projectTestRunner.testFeatureDiscovery.configurers.integration = configurer
    }
}

// Load the custom configurer or compile and load if needed, can't include
// static import requirements here since we might run after a 'clean'
loadGuardTestPhaseConfigurer = {->
    def doLoad = {->
        classLoader.loadClass('guard.grails.GuardTestPhaseConfigurer')
    }
    try {
        doLoad()
    }
    catch(ClassNotFoundException ignored) {
        includeTargets << grailsScript("_GrailsCompile")
        compile()
        doLoad()
    }
}

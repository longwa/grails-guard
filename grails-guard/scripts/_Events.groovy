//noinspection GroovyUnusedAssignment
eventAllTestsStart = {
    if (testOptions['guard']) {
        if (grailsSettings.forkSettings.test) {
            grailsConsole.error("Guard doesn't support forked mode execution (yet), please disabled forked mode for 'test' in BuildConfig.groovy")
            System.exit(1)
        }
        else {
            // Replace the integration configurer with one that loops instead of cleaning up
            def configurer = loadGuardTestPhaseConfigurer().newInstance([projectTestRunner, projectCompiler, projectLoader] as Object[])
            projectTestRunner.testFeatureDiscovery.configurers.integration = configurer
        }
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

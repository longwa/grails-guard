import org.codehaus.groovy.grails.test.junit4.JUnit4GrailsTestType
import org.codehaus.groovy.grails.test.support.GrailsTestMode

guardTests = []
guardSpock = false


loadGuardTestTypes = {
    phasesToRun << "guard"
    def mode = new GrailsTestMode(autowire: true, wrapInTransaction: true, wrapInRequestEnvironment: true)
    guardTests << new JUnit4GrailsTestType("guard", "integration", mode)

    // if spock is loaded
    if (binding.variables.containsKey("loadSpecTestTypeClass")) {
        def specTestTypeClass = loadSpecTestTypeClass()
        guardSpock = true
        guardTests << specTestTypeClass.newInstance('spock', 'integration')
    }
}

// Guard testing uses the same startup as integration
guardTestPhasePreparation = {
    integrationTestPhasePreparation()
}

// Instead of cleaning up, we want to loop
guardTestPhaseCleanUp = {
    includeTargets << new File("${guardPluginDir}/scripts/_Guard.groovy")
    watchForTestChanges()
}

eventAllTestsStart = {
    // Only run guard tests if the phase is explicity. This prevents
    // running guard when doing a grails test-app with no phases given.
    if( targetPhasesAndTypes['guard'] ) {
        loadGuardTestTypes()
    }
}


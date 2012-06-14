import org.codehaus.groovy.grails.test.junit4.JUnit4GrailsTestType
import org.codehaus.groovy.grails.test.support.GrailsTestMode

guardTests = []

loadGuardTestTypes = {
    phasesToRun << "guard"
    def mode = new GrailsTestMode(autowire: true, wrapInTransaction: true, wrapInRequestEnvironment: true)
    guardTests << new JUnit4GrailsTestType("guard", "integration", mode)
}

// Guard testing uses the same startup as integration
guardTestPhasePreparation = {
    integrationTestPhasePreparation()
}

// Instead of cleaning up, we want to loop
guardTestPhaseCleanUp = {
    includeTargets << new File("${basedir}/scripts/_Guard.groovy")
    watchForTestChanges()
}

eventAllTestsStart = {
    loadGuardTestTypes()
}

eventPackagePluginsEnd = {
    loadGuardTestTypes()
}

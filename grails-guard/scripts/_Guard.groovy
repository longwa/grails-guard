import org.codehaus.groovy.grails.cli.ScriptExitException
import org.codehaus.groovy.grails.compiler.DirectoryWatcher
import org.codehaus.groovy.grails.compiler.GrailsProjectWatcher
import org.codehaus.groovy.grails.test.junit4.JUnit4GrailsTestType
import org.codehaus.groovy.grails.test.support.GrailsTestMode

// Configurable wait times
rerunFrequency = Integer.parseInt(System.getProperty("guard.rerun.frequency") ?: "3")
domainWait = Integer.parseInt(System.getProperty("guard.domain.wait") ?: "5")
otherWait = Integer.parseInt(System.getProperty("guard.wait") ?: "2")

target(watchForTestChanges: "Watch for changes") {

    // Must have the reloading agent enabled to do much
    if(GrailsProjectWatcher.isReloadingAgentPresent()) {
        grailsConsole.addStatus "--------------------------------------------------------------------"
        grailsConsole.addStatus "Looping on tests ${testTargetPatterns.rawPattern}..."
        grailsConsole.addStatus "--------------------------------------------------------------------"

        GuardFileChangeListener listener = registerReloadingListener()
        watchLoop(listener, guardSpock)
    }
    else {
        grailsConsole.error "Reloading agent not enabled, try running grails with the -reloading flag as the first argument"
        grailsConsole.error "Example: grails -reloading -echoOut test-app guard: className"
    }
}

/**
 * Class reloading agent
 */
GuardFileChangeListener registerReloadingListener() {
    GuardFileChangeListener listener = new GuardFileChangeListener()
    def watcher = new GrailsProjectWatcher(projectCompiler, pluginManager)

    // Add test directories and a listener so we know when things change
    //noinspection GroovyAssignabilityCheck
    watcher.addWatchDirectory(testSourceDir, "groovy")
    watcher.addListener(listener)
    watcher.start()

    listener
}

/**
 * Main watch loop
 */
def watchLoop(GuardFileChangeListener listener, spock) {

    // Prevent exit on compilation failure
    System.setProperty("grails.disable.exit", "true")

    //noinspection GroovyInfiniteLoopStatement
    while(true) {
        def changes = listener.consumeChanges()
        if( changes ) {
            grailsConsole.addStatus "--------------------------------------------------------------------"
            grailsConsole.addStatus "Detected changes for ${changes*.name.join(',')}, re-running tests..."
            grailsConsole.addStatus "--------------------------------------------------------------------"

            // Check if this is a domain class (quick and dirty check)
            boolean isDomainChange = changes.any { file ->
                file.absolutePath.contains("grails-app/domain") || file.absolutePath.contains("grails-app\\domain")
            }

            // If this is a domain class, wait a little longer before trying to reload
            if( isDomainChange ) {
                grailsConsole.addStatus("Domain class changed, waiting ${domainWait} seconds before running...")
                sleep(domainWait * 1000 as int)
            }

            // See if a reload is still in progress, if so, wait a bit
            while( GrailsProjectWatcher.isReloadInProgress() ) {
                sleep(otherWait * 1000 as int)
            }

            // Run the tests
            def mode = new GrailsTestMode(autowire: true, wrapInTransaction: true, wrapInRequestEnvironment: true)
            def guardTestType = new JUnit4GrailsTestType("guard", "integration", mode)

            // Run the tests
            try {
                currentTestPhaseName = "guard"
                processTests(guardTestType)

                // Run Spock tests
                if (spock) {
                    def specTestTypeClass = classLoader.loadClass('grails.plugin.spock.test.GrailsSpecTestType')
                    def specTestType = specTestTypeClass.newInstance('spock', "integration")
                    processTests(specTestType)
                }
            }
            catch( ScriptExitException e ) {
                grailsConsole.error("Error running tests", e)
            }

            currentTestPhaseName = null

            grailsConsole.addStatus "Tests Complete"
            grailsConsole.addStatus ""
        }

        sleep(rerunFrequency * 1000 as int)
    }
}

/**
 * Listener to collect changes by the reloading agent. I believe it needs to be synchronized
 * since the agent runs in a separate thread and might be fire changes while we are consuming
 * changes.
 */
class GuardFileChangeListener implements DirectoryWatcher.FileChangeListener {
    List<File> changes = []
    synchronized void onChange(File file) {
        changes << file
    }

    synchronized void onNew(File file) {
        changes << file
    }

    synchronized List<File> consumeChanges() {
        List<File> newChanges = changes.clone() as List<File>
        changes.clear()
        newChanges
    }
}

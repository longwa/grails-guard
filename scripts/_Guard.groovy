import org.codehaus.groovy.grails.compiler.DirectoryWatcher
import org.codehaus.groovy.grails.compiler.GrailsProjectWatcher
import org.codehaus.groovy.grails.test.junit4.JUnit4GrailsTestType
import org.codehaus.groovy.grails.test.support.GrailsTestMode

recompileFrequency = Integer.parseInt(System.getProperty("rerun.frequency") ?: "3")

target(watchForTestChanges: "Watch for changes") {

    // Must have the reloading agent enabled to do much
    if(GrailsProjectWatcher.isReloadingAgentPresent()) {
        grailsConsole.addStatus "--------------------------------------------------------------------"
        grailsConsole.addStatus "Looping on tests ${testTargetPatterns.rawPattern}..."
        grailsConsole.addStatus "--------------------------------------------------------------------"

        GuardFileChangeListener listener = registerReloadingListener()
        watchLoop(listener)
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
    watcher.addWatchDirectory(testSourceDir, "groovy")
    watcher.addListener(listener)
    watcher.start()

    listener
}

/**
 * Main watch loop
 */
def watchLoop(GuardFileChangeListener listener) {
    //noinspection GroovyInfiniteLoopStatement
    while(true) {
        def changes = listener.consumeChanges()
        if( changes ) {
            grailsConsole.addStatus "--------------------------------------------------------------------"
            grailsConsole.addStatus "Detected changes for ${changes*.name.join(',')}, re-running tests..."
            grailsConsole.addStatus "--------------------------------------------------------------------"
            sleep(1000)

            // Run the tests
            def mode = new GrailsTestMode(autowire: true, wrapInTransaction: true, wrapInRequestEnvironment: true)
            def guardTestType = new JUnit4GrailsTestType("guard", "integration", mode)

            // Run the tests
            currentTestPhaseName = "guard"
            processTests(guardTestType)
            currentTestPhaseName = null

            grailsConsole.addStatus "Tests Complete"
            grailsConsole.addStatus ""
        }

        sleep(recompileFrequency * 1000)
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

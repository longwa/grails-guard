package guard.grails

import grails.build.logging.GrailsConsole
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.project.compiler.GrailsProjectCompiler
import org.codehaus.groovy.grails.project.compiler.GrailsProjectWatcher
import org.codehaus.groovy.grails.project.loader.GrailsProjectLoader
import org.codehaus.groovy.grails.test.runner.GrailsProjectTestRunner
import org.codehaus.groovy.grails.test.runner.phase.IntegrationTestPhaseConfigurer
import org.codehaus.groovy.grails.test.spock.GrailsSpecTestType
import org.codehaus.groovy.grails.test.support.GrailsTestMode

/**
 * Intercept the normal integration test cleanup step and loop, waiting for
 * changes to either application code or the tests.
 *
 * @author Aaron Long
 * @since 2.0.0
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class GuardTestPhaseConfigurer extends IntegrationTestPhaseConfigurer {
    GrailsConsole grailsConsole = GrailsConsole.getInstance()

    protected GrailsProjectTestRunner testRunner
    protected GuardFileChangeListener listener
    protected GrailsProjectCompiler projectCompiler

    protected long domainExtraDelay = 5000
    protected long compileDelay = 3000
    protected long pollingInterval = 1000

    private reloadNotified = false

    GuardTestPhaseConfigurer(GrailsProjectTestRunner testRunner, GrailsProjectCompiler projectCompiler, GrailsProjectLoader projectLoader) {
        super(testRunner.projectTestCompiler, projectLoader)
        this.testRunner = testRunner
        this.projectCompiler = projectCompiler

        grailsConsole.addStatus("Initializing Guard...")

        projectWatcher = new GrailsProjectWatcher(projectCompiler, null)
        listener = new GuardFileChangeListener()

        // Add test directories and a listener so we know when things change
        projectWatcher.addWatchDirectory(testRunner.testSourceDir, "groovy")
        projectWatcher.addListener(listener)
    }

    /**
     * Instead of exiting normally we want to loop and process changes
     *
     * @param testExecutionContext
     * @param testOptions
     */
    @Override
    void cleanup(Binding testExecutionContext, Map<String, Object> testOptions) {
        if(GrailsProjectWatcher.isReloadingAgentPresent()) {
            projectWatcher.start()
            watchLoop()
        }
        else {
            grailsConsole.error "The reloading agent is not enabled, try running grails with the '-reloading' flag as the first argument"
            grailsConsole.error "Ex:"
            grailsConsole.error "  grails -reloading -guard test-app integration: className"
        }

        super.cleanup(testExecutionContext, testOptions)
    }

    /**
     * Main watch loop
     */
    void watchLoop() {
        grailsConsole.addStatus "Looping on tests ${testRunner.testTargetPatterns.rawPattern}..."

        // Prevent exit on compilation failure
        System.setProperty("grails.disable.exit", "true")

        //noinspection GroovyInfiniteLoopStatement
        while(true) {
            def changes = listener.consumeChanges(compileDelay)
            if(changes) {
                grailsConsole.addStatus "--------------------------------------------------------------------"
                grailsConsole.addStatus "Detected changes for ${changes*.name.join(',')}, re-running tests..."
                grailsConsole.addStatus "--------------------------------------------------------------------"

                // Check if this is a domain class (quick and dirty check)
                boolean isDomainChange = changes.any { File file ->
                    file.absolutePath.contains("grails-app/domain") || file.absolutePath.contains("grails-app\\domain")
                }

                // If this is a domain class, wait a little longer before trying to reload
                if(isDomainChange) {
                    grailsConsole.addStatus("Domain class changed, waiting extra ${domainExtraDelay}ms before running...")

                    def app = appCtx.getBean(GrailsApplication.APPLICATION_ID, GrailsApplication)
                    if (app.config.hibernate.reload != false && !reloadNotified) {
                        grailsConsole.addStatus("NOTE: Domain class reloading may be more reliable if you disable session factory reloading")
                        grailsConsole.addStatus("To disable set hibernate.reload = false in DataSource.groovy")
                        reloadNotified = true
                    }

                    sleep(domainExtraDelay)
                }

                // Run the tests
                def mode = new GrailsTestMode(autowire: true, wrapInTransaction: true, wrapInRequestEnvironment: true)
                def guardTestType = new GrailsSpecTestType("integration", "integration", mode)

                // Run the tests
                try {
                    testRunner.testExecutionContext.setVariable("currentTestPhaseName", "integration")
                    testRunner.processTests(guardTestType)
                }
                catch(e) {
                    grailsConsole.error("Error running tests", e)
                }
                finally {
                    testRunner.testExecutionContext.setVariable("currentTestPhaseName", null)
                }

                grailsConsole.addStatus "Tests Complete"
                grailsConsole.addStatus ""
            }

            sleep(pollingInterval)
        }
    }
}

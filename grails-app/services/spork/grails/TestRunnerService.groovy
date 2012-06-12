package spork.grails

import grails.util.BuildSettings
import grails.util.BuildSettingsHolder

import org.codehaus.groovy.grails.test.GrailsTestTargetPattern
import org.codehaus.groovy.grails.test.event.GrailsTestEventPublisher
import org.codehaus.groovy.grails.test.support.GrailsTestMode

import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

/**
 * Service to execute a grails integration test within a running server environment
 *
 * @author Aaron Long
 */
class TestRunnerService {
    static transactional = false

    def grailsApplication

    /**
     * Run the given test using the grails test patterns accepted by test-app
     *
     * @param testPattern ex. [grails.spork.*, Classpath, SampleService]
     */
    SporkGrailsTestTypeResultAdapter runTests(testPattern) {
        SporkGrailsTestTypeResultAdapter results = null
        Exception error = null

        log.info("Executing test pattern(s): ${testPattern.toString()}")

        // Since the test runner manipulates the class loader for the current thread, we need to run
        // in a new thread to prevent losing some of the context for our thread. Without running in a new
        // thread, it seems to lose the request context for the controller among other weird things.
        def thread = Thread.start {
            // Should be able to process any normal test pattern
            def testTargetPatterns = testPattern.collect { new GrailsTestTargetPattern(it) } as GrailsTestTargetPattern[]

            // Run in the same mode as normal integration tests
            def mode = new GrailsTestMode(autowire: true, wrapInTransaction: true, wrapInRequestEnvironment: true)
            def testType = new SporkGrailsTestType("integration", "integration", mode)

            // Setup and run the tests
            try {
                def count = testType.prepare(
                        testTargetPatterns,
                        new File("${BuildSettingsHolder.settings.testClassesDir}/integration"),
                        createBuildBinding()
                )

                if( count ) {
                    // Since we aren't running as part of a build script, just mock out the event publisher
                    def mockEventPublisher = new GrailsTestEventPublisher({ eventName, name -> })

                    // Run the tests and collect results
                    results = testType.run(mockEventPublisher) as SporkGrailsTestTypeResultAdapter
                }
                else {
                    log.error("No tests found to run for ${testPattern.toString()}")
                }
            }
            catch(Exception e) {
                log.error("Error executing tests", e)
                error = e
            }
        }

        // Wait for the test thread to complete
        thread.join()

        // If there was an error running the tests, rethrow it from this thread. This doesn't include assertion errors
        // and things from within the tests, but rather errors actually trying to start the tests.
        if( error != null ) {
            throw error
        }

        results
    }

    /**
     * Create a groovy binding to satisfy the default grails test type
     */
    private Binding createBuildBinding() {

        // Resource resolver
        def resolver = { pattern ->
            try {
                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver()
                return resolver.getResources(pattern)
            }
            catch(Exception ignored) {
                return new Resource[0]
            }
        }

        // Need to mock the GantBinding in order to use the existing test wrappers
        Binding binding = new Binding()
        BuildSettings bs = BuildSettingsHolder.settings

        // Create a new class loader for the test run to use
        URLClassLoader classLoader = new URLClassLoader(
                [new File("${bs.testClassesDir}/integration").toURL()] as URL[],
                grailsApplication.classLoader
        )

        def reportFormats = ["xml", "plain"]

        // Setup bindings needed by test code
        binding.setVariable("grailsSettings", bs)
        binding.setVariable("resolveResources", resolver)
        binding.setVariable("classLoader", classLoader)
        binding.setVariable("currentTestPhaseName", "integration")
        binding.setVariable("currentTestTypeName", "integration")
        binding.setVariable("testReportsDir", ensureTestDirectories(bs.testReportsDir, reportFormats))
        binding.setVariable("testOptions", [echoOut: true, echoErr: true])
        binding.setVariable("reportFormats", reportFormats)
        binding.setVariable("appCtx", grailsApplication.mainContext)
        binding
    }

    /**
     * Since the user may not have run test-app, some of the target directories may not exist.
     *
     * @param testReportsDir
     * @param reportFormats
     * @return
     */
    private File ensureTestDirectories(File testReportsDir, reportFormats) {
        if( !testReportsDir.exists() ) {
            testReportsDir.mkdirs()
        }

        // Create sub-directories for reports (if needed)
        reportFormats.each { new File(it, testReportsDir).mkdirs() }
        testReportsDir
    }
}

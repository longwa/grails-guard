package spork.grails



import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(TestRunnerService)
class TestRunnerServiceTests {

    void testCreateBinding() {
        def binding = service.createBuildBinding()
        assert binding != null
    }

    void testRunTests() {
        def results = service.runTests(["Classpath"])
        assert results != null
    }
}

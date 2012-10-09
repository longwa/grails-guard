class GuardGrailsPlugin {
    // the plugin version
    def version = "1.0.7"

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"

    // the other plugins this plugin depends on
    def dependsOn = [:]

    def loadAfter = ['spock']

    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // Only deploy in development and test environments
    def environments = ['development', 'test']

    // Never make this part of a war file
    def scopes = [excludes:'war']

    def title = "Guard Plugin"
    def author = "Aaron Long"
    def authorEmail = "longwa@gmail.com"
    def description = 'Provides a way to run integration tests without having to repeatedly bootstrap the environment'

    // URL to the plugin's documentation
    def documentation = "https://github.com/longwa/grails-guard/blob/master/README.md"

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/longwa/grails-guard" ]
}

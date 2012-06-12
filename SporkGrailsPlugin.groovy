class SporkGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "Spork Grails Plugin" // Headline display name of the plugin
    def author = "Aaron Long"
    def authorEmail = "longwa@gmail.com"
    def description = '''\
Provides a way to execute integration tests within a running server instance
'''
    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/spork"

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/longwa/grails-spork" ]
}

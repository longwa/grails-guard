grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6

grails.release.scm.enabled = false

grails.project.dependency.resolution = {
    inherits("global") {
    }

    log "warn" 
    repositories {
      grailsRepo "http://grails.org/plugins"
    }

    dependencies {
        compile('org.codehaus.groovy.modules.http-builder:http-builder:0.5.2') {
            excludes "commons-logging", "xml-apis", "groovy"
        }
    }

    plugins {
        build(":tomcat:$grailsVersion", ":release:2.0.3") {
            export = false
        }
    }
}

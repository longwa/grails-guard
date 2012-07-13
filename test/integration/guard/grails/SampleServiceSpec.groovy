package guard.grails
import grails.plugin.spock.IntegrationSpec

import spock.lang.*

class SampleServiceSpec extends IntegrationSpec {

	def sampleService

	def "injected sampleService is present"() {
	    when:
	    	def msg = sampleService.helloWorld()

	    then:
	    	msg == "Hello World!"
  }
}
package test

import grails.test.spock.IntegrationSpec

class AuthorSpec extends IntegrationSpec {
    void "Test something on Author"() {
        given:
        def author = new Author(name: 'Aaron', age: 29)

        when:
        author.save(flush: true, failOnError: true)

        then:
        author.id
        author.doSomething() == 'something'
    }
}

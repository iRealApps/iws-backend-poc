package iws.backend

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import grails.testing.spock.OnceBefore
import spock.lang.Shared
import iws.backend.Utils

@Integration
@Rollback
class ApiSpec extends Specification {
  @Shared
  String baseUrl

  @Shared
  String sessionId

  @OnceBefore
  void init() {
    baseUrl = "http://localhost:$serverPort/api"
  }

  def setup() {
  }

  def cleanup() {
  }

  void 'New user can be created'() {
    when:
    def result = Utils.restCall("${baseUrl}/user",
        Utils.httpMethod.post,
        [
            loginname: 'manoj@iriplco.com',
            password : 'Pwd1234#',
            name     : 'Manoj Kumar'
        ]
    )
    Map response = result.response as Map

    then:
    result.status == Utils.httpStatus.success
    response.sessionId != null
    response.name == "Manoj Kumar"
  }

  // TODO: Validations

  void 'User can create a session'() {
    when:
    def result = Utils.restCall("${baseUrl}/session",
        Utils.httpMethod.post,
        [
            loginname: 'manoj@iriplco.com',
            password : 'Pwd1234#'
        ]
    )
    Map response = result.response as Map
    sessionId = response.sessionId

    then:
    result.status == Utils.httpStatus.success
    response.sessionId != null
    response.name == "Manoj Kumar"
  }

  void 'User can delete an existing session'() {
    when:
    def result = Utils.restCall("${baseUrl}/session",
        Utils.httpMethod.delete,
        [:],
        ['Authorization': "Basic ${sessionId}"]
    )
    Map response = result.response as Map

    then:
    result.status == Utils.httpStatus.success
  }

}

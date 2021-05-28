package iws.backend

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import grails.testing.spock.OnceBefore
import spock.lang.Shared
import iws.backend.Utils
import grails.core.GrailsApplication

@Integration
@Rollback
class ApiSpec extends Specification {

  GrailsApplication grailsApplication
  ConversationEngineService conversationEngineService

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

  void 'Guest session can be created'() {
    when:
    String guestName = grailsApplication.config.getProperty('grails.guest-name')
    def result = Utils.restCall("${baseUrl}/session",
        Utils.httpMethod.post,
        [
            loginname: 'guest'
        ]
    )
    Map response = result.response as Map
    sessionId = response.sessionId

    then:
    result.status == Utils.httpStatus.success
    response.sessionId != null
    response.name == guestName
  }

  void 'Step can be created'() {
    when:
    def stepData = [
        isDefault: true,
        name     : 'greet',
        details  : '{"message": "Hello"}',
        action   : '{"type":"goto", "target": "end"}'
    ]
    Step step = conversationEngineService.createStep(stepData)

    then:
    step != null
    step.isDefault == stepData.isDefault
    step.name == stepData.name
    step.details == stepData.details
    step.action == stepData.action
  }

  //  void 'Current step can be fetched'() {
  //    when:
  //    def result = Utils.restCall("${baseUrl}/step",
  //        Utils.httpMethod.get,
  //        [:],
  //        ['Authorization': "Basic ${sessionId}"]
  //    )
  //    Map response = result.response as Map
  //
  //    then:
  //    result.status == Utils.httpStatus.success
  //  }
  //
  //  void 'Current step can be completed'() {
  //    when:
  //    def result = Utils.restCall("${baseUrl}/step",
  //        Utils.httpMethod.put,
  //        [:],
  //        ['Authorization': "Basic ${sessionId}"]
  //    )
  //    Map response = result.response as Map
  //
  //    then:
  //    result.status == Utils.httpStatus.success
  //  }

}

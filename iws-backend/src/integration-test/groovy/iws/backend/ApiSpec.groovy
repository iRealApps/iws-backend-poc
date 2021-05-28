package iws.backend

import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import grails.testing.spock.OnceBefore
import spock.lang.Shared
import iws.backend.Utils
import grails.core.GrailsApplication
import spock.lang.Stepwise
import grails.gorm.transactions.Transactional

@Integration
@Stepwise
class ApiSpec extends Specification {

  GrailsApplication grailsApplication
  ConversationEngineService conversationEngineService

  @Shared
  String baseUrl

  @Shared
  Step defaultStep

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

  void '1. New user can be created'() {
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

  void '2. User can create a session'() {
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

  void '3. User can delete an existing session'() {
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

  void '4. Guest session can be created'() {
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

  void '5. Step can be created'() {
    when:
    def stepData = [
        isDefault: true,
        name     : 'begin',
        details  : '{"message": "Hello"}',
        action   : '{"type":"goto", "target": "end"}'
    ]
    defaultStep = conversationEngineService.createStep(stepData)

    then:
    defaultStep != null
    defaultStep.isDefault == stepData.isDefault
    defaultStep.name == stepData.name
    defaultStep.details == stepData.details
    defaultStep.action == stepData.action
  }

  void '6. Default user step should be fetched'() {
    when:
    def result = Utils.restCall("${baseUrl}/user/step",
        Utils.httpMethod.get,
        [:],
        ['Authorization': "Basic ${sessionId}"]
    )
    Map response = result.response as Map

    then:
    result.status == Utils.httpStatus.success
    response.step.name == defaultStep.name
    response.step.details == defaultStep.details
  }

  void '7. Should be able to move to next step'() {
    when:
    def stepData = [
        isDefault: false,
        name     : 'end',
        details  : '{"message": "Bye"}',
        action   : '{}'
    ]
    defaultStep = conversationEngineService.createStep(stepData)
    def result = Utils.restCall("${baseUrl}/user/step",
        Utils.httpMethod.put,
        [input: '{}'],
        ['Authorization': "Basic ${sessionId}"]
    )
    Map response = result.response as Map

    then:
    result.status == Utils.httpStatus.success
    response.step.name == stepData.name
    response.step.details == stepData.details
  }

  void '8. After moving to next step, new step should be fetched'() {
    when:
    def result = Utils.restCall("${baseUrl}/user/step",
        Utils.httpMethod.get,
        [:],
        ['Authorization': "Basic ${sessionId}"]
    )
    Map response = result.response as Map

    then:
    result.status == Utils.httpStatus.success
    response.step.name == 'end'
  }

}

package iws.backend

import grails.rest.*
import grails.converters.*
import iws.backend.*
import iws.backend.User
import grails.core.GrailsApplication
import groovy.json.JsonSlurper

class ConversationEngineController extends BaseController {
  static responseFormats = ['json']

  GrailsApplication grailsApplication
  ConversationEngineService conversationEngineService

  ConversationEngineController() {
    super(User)
  }

  def createUser() {
    String requestBody = request.reader.text
    Utils.ensureRequestBodyExists(requestBody)
    def input = Utils.getMapFromString(requestBody) as Map
    logAPI "createUser", (Utils.getSubObjectAsMap(input, ['loginname', 'name'])) as JSON
    def result = conversationEngineService.createUser(input, grailsApplication, request)
    render([sessionId: result.session.id, name: result.user.name] as JSON)
  }

  def createSession() {
    String requestBody = request.reader.text
    Utils.ensureRequestBodyExists(requestBody)
    def input = Utils.getMapFromString(requestBody) as Map
    logAPI "createSession", (Utils.getSubObjectAsMap(input, ['loginname'])) as JSON
    if (input.loginname == 'guest') {
      input.loginname = grailsApplication.config.getProperty('grails.guest-loginname')
      input.password = grailsApplication.config.getProperty('grails.guest-password')
    }
    def result = conversationEngineService.createSession(input, grailsApplication, request)
    if (result.keySet().contains('session')) {
      render([sessionId: result.session.id, name: result.session.user.name] as JSON)
    } else
      render Utils.removeEmptyKeys(apiResult) as JSON
  }

  def deleteSession() {
    String sessionId = getSessionId(request)
    logAPI "deleteSession", sessionId
    conversationEngineService.deleteSession(sessionId)
    render [:] as JSON
  }

  Map getStepAsMap(Step step) {
    return [
      name: step.name,
      details: new JsonSlurper().parseText(step.details)
    ]
  }

  def getUserStep() {
    String sessionId = getSessionId(request)
    logAPI "getUserStep", sessionId
    Map result = conversationEngineService.getUserStep(sessionId)
    render([
        step   : getStepAsMap(result.step),
        context: [name: result.user.name]
    ] as JSON)
  }

  def putUserStep() {
    String sessionId = getSessionId(request)
    String requestBody = request.reader.text
    Utils.ensureRequestBodyExists(requestBody)
    def input = Utils.getMapFromString(requestBody) as Map
    logAPI "putUserStep", sessionId
    Map result = conversationEngineService.putUserStep(sessionId, input)
    render([
        step   : getStepAsMap(result.step),
        context: [name: result.user.name]
    ] as JSON)
  }

}

package iws.backend

import grails.rest.*
import grails.converters.*
import iws.backend.*
import iws.backend.User
import grails.core.GrailsApplication

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

  def getStep() {
    render(conversationEngineService.getStep() as JSON)
  }

  def putStep() {
    render(conversationEngineService.putStep() as JSON)
  }

}

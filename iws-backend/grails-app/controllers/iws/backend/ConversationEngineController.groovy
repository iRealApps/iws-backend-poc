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
    logAPI "creatUser", (Utils.getSubObjectAsMap(input, ['loginname', 'name'])) as JSON
    def result = conversationEngineService.createUser(input, grailsApplication)
    render([sessionId: result.session.id, name: result.user.name] as JSON)
  }

  def createSession() {
    render(conversationEngineService.createSession() as JSON)
  }

  def deleteSession() {
    render(conversationEngineService.deleteSession() as JSON)
  }

  def getStep() {
    render(conversationEngineService.getStep() as JSON)
  }

  def putStep() {
    render(conversationEngineService.putStep() as JSON)
  }

}

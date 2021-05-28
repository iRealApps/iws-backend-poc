package iws.backend

import grails.converters.JSON
import grails.rest.*
import iws.backend.AppException
import iws.backend.AuthException
import iws.backend.LogService
import iws.backend.Utils

class BaseController extends RestfulController {
  static responseFormats = ['json', 'png']

  LogService logService
  Class cls

  BaseController() {
    super(User)
  }

  BaseController(Class cls) {
    super(cls)
    this.cls = cls
  }

  void logAPI(String name, def details = '') {
    logService.logInfo "API: ${name}: ${details}"
  }

  void throwAppException(String userMessage, String category = '', String logMessage = '') {
    throw new AppException(userMessage, category, logMessage)
  }

  def String getSessionId(request) {
    String header = request.getHeader('Authorization')
    return Utils.getSessionId(header)
  }

  def String getAdminSessionId(request) {
    String header = request.getHeader('Authorization')
    return Utils.getAdminSessionId(header)
  }

  def onAppException(AppException appException) {
    response.setStatus(400)
    logService.logError(
        appException.id,
        cls.name,
        appException.category,
        appException.message,
        appException.logMessage
    )
    def exception = Utils.getErrorAsMap(appException) as JSON
    render exception
  }

  def onAuthException(AuthException authException) {
    response.setStatus(401)
    logService.logError(
        authException.id,
        cls.name,
        authException.category,
        authException.message,
        authException.logMessage
    )
    def exception = Utils.getErrorAsMap(authException) as JSON
    render exception
  }

  def onException(Exception unknownException) {
    response.setStatus(500)
    AppException ex = new AppException(
        Utils.isTestEnvironment() ? unknownException.message : 'Internal Error',
        'unknown',
        unknownException.message
    )
    logService.logError(
        ex.id,
        cls.name,
        ex.category,
        ex.message,
        ex.logMessage,
        unknownException
    )
    def exception = Utils.getErrorAsMap(ex) as JSON
    render exception
  }

}

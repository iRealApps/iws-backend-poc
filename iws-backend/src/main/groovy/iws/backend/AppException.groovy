package iws.backend

class AppException extends Exception {

  String id
  String category
  String logMessage

  AppException(String userMessage, String category = '', String logMessage = '') {
    super(userMessage)
    this.id = Utils.newId()
    this.category = category
    this.logMessage = logMessage
  }
}

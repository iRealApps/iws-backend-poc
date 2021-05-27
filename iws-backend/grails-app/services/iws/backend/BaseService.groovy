package iws.backend

class BaseService {

  LogService logService

  void throwAppException(String userMessage, String category = '', String logMessage = '') {
    throw new AppException(userMessage, category, logMessage)
  }

  void logInfo(String info) {
    logService.logInfo(info)
  }

}

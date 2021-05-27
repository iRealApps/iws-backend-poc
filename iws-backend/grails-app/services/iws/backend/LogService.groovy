package iws.backend

class LogService {

  void logInfo(String info) {
    log.info info
  }

  void logError(
      String id,
      String where,
      String category,
      String userMessage,
      String logMessage,
      Exception ex = null
  ) {
    log.error "${id}: ${where} | ${category} | ${userMessage} | ${logMessage}"
    if (ex) log.error "${id}::\n  ${ex.stackTrace.join('\n  ')}"
  }

}

package iws.backend

import grails.gorm.transactions.Transactional

class ConversationEngineService extends BaseService {

  @Transactional
  Map createUser(Map input, def grailsApplication, def request) {
    if (input.password) {
      if (!Utils.isPasswordStrong(input.password)) {
        throwAppException('Weak password', 'validation',
            "Please enter valid password")
      }
    }
    if (input.loginname.trim() == '')
      throwAppException('Invalid loginname', 'validation', "Please enter valid loginname")
    User existingUser = User.find {
      loginname == Utils.toLowerCase(input.loginname)
    }
    if (existingUser) throwAppException('User exists', 'validation',
        "This user already exists: ${input.loginname}")
    User user = new User(input)
    user.password = Utils.encryptString(input.password)
    user.createdAt = Utils.getNow()
    if (user.validate()) {
      user.save(flush: true)
      return [user: user, session: createSessionObject(user, request)]
    } else throwAppException(AppException.type.invalidInput, 'validation',
        user.errors.allErrors.join('|'))
  }

  private void throwLoginException() {
    throwAppException('Invalid Credentials', 'login')
  }

  @Transactional
  Map createSession(Map input, def grailsApplication, def request) {
    String loginname = input["loginname"].toString().toLowerCase()
    String password = input["password"]
    User user = User.find { loginname == loginname }
    if (!user) throwLoginException()
    if (!Utils.isValidEncryptedString(password, user.password))
      throwLoginException()

    return [session: createSessionObject(user, request)]
  }

  private createSessionObject(User user, def request) {
    def now = Utils.getNow()
    Session newSession = new Session(
        user: user,
        createdAt: now,
        lastActivityAt: now,
        state: Session.ObjectState.active,
        userAgent: request.getHeader("User-Agent")
    )
    if (newSession.validate()) {
      newSession.save(flush: true)
      return newSession
    } else {
      throwAppException(AppException.type.invalidInput, 'validation', newSession.errors.allErrors.join('|'))
    }
  }

  private void throwInvalidSessionException() {
    throw new AuthException("Invalid session")
  }

  public Session getActiveSession(String sessionId) {
    Session session = Session.get(sessionId)
    if (!session)
      throwInvalidSessionException()
    if (session.state != Session.ObjectState.active)
      throwInvalidSessionException()
    return session
  }

  @Transactional
  private void closeSession(Session session) {
    session.closedAt = Utils.getNow()
    session.lastActivityAt = session.closedAt
    session.state = Session.ObjectState.closed
    session.save(flush: true)
  }

  @Transactional
  def void deleteSession(String sessionId) {
    Session session = getActiveSession(sessionId)
    closeSession(session)
  }

  Map getStep() {
    throwAppException('Not implemented')
  }

  Map putStep() {
    throwAppException('Not implemented')
  }

}

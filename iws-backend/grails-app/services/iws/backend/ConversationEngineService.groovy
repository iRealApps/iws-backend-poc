package iws.backend

import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper

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
    } else throwAppException('Invalid input', 'validation',
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
      throwAppException('Invalid input', 'validation',
          newSession.errors.allErrors.join('|'))
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

  @Transactional
  def createStep(Map input) {
    Step step = new Step(input)
    if (step.validate()) {
      step.save(flush: true)
      return step
    } else throwAppException('Invalid input', 'validation',
        step.errors.allErrors.join('|'))
  }

  @Transactional
  Flow createDefaultFlow(User user) {
    Flow flow = new Flow([
        user     : user,
        step     : Step.find { isDefault == true },
        prev     : null,
        next     : null,
        createdAt: Utils.getNow(),
        details  : null
    ])
    if (flow.validate()) {
      flow.save(flush: true)
      return flow
    } else throwAppException('Could not create flow', 'error',
        flow.errors.allErrors.join('|'))
  }

  @Transactional
  Flow createNewFlow(User user, Step step, Flow prev, String details) {
    Flow flow = new Flow([
        user     : user,
        step     : step,
        prev     : prev,
        next     : null,
        createdAt: Utils.getNow(),
        details  : details
    ])
    if (flow.validate()) {
      flow.save(flush: true)
      return flow
    } else throwAppException('Could not create flow', 'error',
        flow.errors.allErrors.join('|'))
  }

  Step findNextStep(User user, Step currentStep, def input) {
    // TODO: Implement the logic engine
    println ">>> ${currentStep.name}, ${currentStep.action}"
    String targetStepName = (new JsonSlurper().parseText(currentStep.action)).target
    return Step.find { name == targetStepName }
  }

  Flow getUserActiveFlow(User user) {
    return Flow.find { user == user && next == null }
  }

  Map getUserStep(String sessionId) {
    Session session = getActiveSession(sessionId)
    User me = session.user
    Flow flow = getUserActiveFlow(me)
    if (!flow) {
      flow = createDefaultFlow(me)
    }
    return [step: flow.step, user: me, session: session]
  }

  @Transactional
  Map putUserStep(String sessionId, Map input) {
    Session session = getActiveSession(sessionId)
    User me = session.user
    Flow flow = getUserActiveFlow(me)
    if (!flow) throwAppException('Could not find current flow', 'error')
    flow.details = input.details
    Flow newFlow = createNewFlow(
        me,
        findNextStep(me, flow.step, input.input),
        flow,
        input.input
    )
    flow.next = newFlow
    if (flow.validate()) {
      flow.save(flush: true)
      return [step: newFlow.step, user: me, session: session]
    } else throwAppException('Could not update flow', 'error',
        flow.errors.allErrors.join('|'))
  }

}

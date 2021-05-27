package iws.backend

import grails.gorm.transactions.Transactional

class ConversationEngineService extends BaseService {

  @Transactional
  Map createUser(Map input, def grailsApplication) {
    if (input.password) {
      if (!Utils.isPasswordStrong(input.password)) {
        throwAppException('Weak password', 'validation',
            "Please Enter Valid Password")
      }
    }
    User existingUser = User.find {
      loginname == Utils.toLowerCase(input.loginname)
    }
    if (existingUser) throwAppException('User exists', 'validation',
        "This user already exists: ${input.loginname}")
    User user = new User(input)
    user.createdAt = Utils.getNow()
    if (user.validate()) {
      user.save(flush: true)
      // TODO: Create Session
      return [user: user, session: [id: "avdd"]]
    } else throwAppException(AppException.type.invalidInput, 'validation',
        user.errors.allErrors.join('|'))
  }

  Map createSession() {
    return [:]
  }

  Map deleteSession() {
    return [:]
  }

  Map getStep() {
    return [:]
  }

  Map putStep() {
    return [:]
  }

}

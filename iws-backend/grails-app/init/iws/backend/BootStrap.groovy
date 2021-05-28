package iws.backend

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional

class BootStrap {

  GrailsApplication grailsApplication

  def init = { servletContext ->
    ensureGuestUserExists()
  }

  @Transactional
  def ensureGuestUserExists() {
    String guestLoginname = grailsApplication.config.getProperty('grails.guest-loginname')
    String guestPassword = grailsApplication.config.getProperty('grails.guest-password')
    String guestName = grailsApplication.config.getProperty('grails.guest-name')
    User user = User.find { loginname == guestLoginname }
    if (!user) {
      user = new User([loginname: guestLoginname, name: guestName])
      user.password = Utils.encryptString(guestPassword)
      user.createdAt = Utils.getNow()
      if (user.validate()) {
        user.save(flush: true)
      }
    }
  }

  def destroy = {
  }

}

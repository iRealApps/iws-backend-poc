package iws.backend

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import grails.testing.spock.OnceBefore
import spock.lang.Shared

@Integration
@Rollback
class ApiSpec extends Specification {
  @Shared
  String baseUrl

  @OnceBefore
  void init() {
    baseUrl = "http://localhost:$serverPort/api"
  }

  def setup() {
  }

  def cleanup() {
  }

  void 'New user can be created'() {
    when:
    def result = Utils.restCall("${baseUrl}/user",
        Utils.httpMethod.post, [
        loginname: 'manoj@iriplco.com',
        password : 'Pwd1234#',
        name     : 'Manoj Kumar'
    ])
    int status = result.status
    Map response = result.response as Map

    then:
    response == []
    status == Utils.httpStatus.success
  }

  // TODO: Test validations


}

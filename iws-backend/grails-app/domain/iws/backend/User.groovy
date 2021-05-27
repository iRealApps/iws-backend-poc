package iws.backend

import java.time.LocalDateTime

class User {
  String id
  String name
  String loginname
  String password
  LocalDateTime createdAt

  static mapping = {
    table '`User`'
    password column: '`password`'
    id generator: 'uuid'
  }

  static constraints = {
    loginname email: true, unique: true, blank: false, nullable: false
    password unique: false, blank: true, nullable: true, password: true
    name unique: false, blank: true, nullable: true
    createdAt unique: false, blank: true, nullable: true
  }

}

package iws.backend

import java.time.LocalDateTime

class Flow {

  String id
  User user
  Step step
  Flow prev
  Flow next
  LocalDateTime createdAt
  String details

  static mapping = {
    table '`Flow`'
    id generator: 'uuid'
  }

  static constraints = {
    user unique: false, blank: true, nullable: true
    step unique: false, blank: false, nullable: false
    prev unique: false, blank: true, nullable: true
    next unique: false, blank: true, nullable: true
    createdAt unique: false, blank: true, nullable: true
    details unique: false, blank: true, nullable: true, sqlType: 'text'
  }

}

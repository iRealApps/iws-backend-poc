package iws.backend

import java.time.LocalDateTime
import iws.backend.Utils

class Session {
  static ObjectState = [
      'active'   : 'active',
      'closed'   : 'closed',
      'timed-out': 'timed-out'
  ]

  String id
  User user
  LocalDateTime createdAt
  LocalDateTime lastActivityAt
  LocalDateTime closedAt
  String state
  String userAgent

  static mapping = {
    table '`Session`'
    id generator: 'uuid'
  }

  static constraints = {
    user unique: false, blank: true, nullable: true
    createdAt unique: false, blank: true, nullable: true
    lastActivityAt unique: false, blank: true, nullable: true
    closedAt unique: false, blank: true, nullable: true
    state unique: false, blank: false, nullable: false,
        inList: Utils.getKeys(Session.ObjectState)
    userAgent unique: false, blank: true, nullable: true
  }

}

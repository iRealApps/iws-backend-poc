package iws.backend

class Step {

  String id
  Boolean isDefault = false
  String name
  String details
  String action

  static mapping = {
    table '`Step`'
    id generator: 'uuid'
  }

  static constraints = {
    isDefault unique: false, blank: false, nullable: false
    name unique: true, blank: true, nullable: true
    details unique: false, blank: true, nullable: true, sqlType: 'text'
    action unique: false, blank: true, nullable: true, sqlType: 'text'
  }

}

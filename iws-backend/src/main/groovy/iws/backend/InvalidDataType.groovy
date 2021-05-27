package iws.backend

class InvalidDataType extends Exception {

  InvalidDataType(String message) {
    super("invalid datatype: ${message}")
  }
}

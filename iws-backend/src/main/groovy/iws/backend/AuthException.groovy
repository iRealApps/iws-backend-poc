package iws.backend

class AuthException extends AppException {
  AuthException(String message) {
    super(message, "authorization")
  }
}

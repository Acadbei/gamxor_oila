sealed class AppException implements Exception {
  const AppException(this.message);

  final String message;

  @override
  String toString() => message;
}

final class NetworkException extends AppException {
  const NetworkException([super.message = 'Network request failed.']);
}

final class UnauthorizedException extends AppException {
  const UnauthorizedException([super.message = 'Unauthorized request.']);
}

final class ValidationException extends AppException {
  const ValidationException([super.message = 'Invalid request payload.']);
}

final class ServerException extends AppException {
  const ServerException([super.message = 'Server error.']);
}

final class UnknownException extends AppException {
  const UnknownException([super.message = 'Unknown error.']);
}

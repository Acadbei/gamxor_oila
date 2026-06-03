sealed class Failure {
  const Failure(this.message);

  final String message;
}

final class NetworkFailure extends Failure {
  const NetworkFailure([super.message = 'Network request failed.']);
}

final class ValidationFailure extends Failure {
  const ValidationFailure([super.message = 'Validation failed.']);
}

final class UnauthorizedFailure extends Failure {
  const UnauthorizedFailure([super.message = 'Unauthorized request.']);
}

final class ServerFailure extends Failure {
  const ServerFailure([super.message = 'Server error.']);
}

final class UnknownFailure extends Failure {
  const UnknownFailure([super.message = 'Unknown error.']);
}

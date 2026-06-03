import '../../../../core/errors/result.dart';
import '../entities/email_otp_challenge.dart';
import '../repositories/auth_repository.dart';

class RequestEmailOtpUseCase {
  const RequestEmailOtpUseCase(this._repository);

  final AuthRepository _repository;

  Future<Result<EmailOtpChallenge>> call(String email) {
    return _repository.requestOtp(email);
  }
}

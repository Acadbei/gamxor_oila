import '../../../../core/errors/result.dart';
import '../repositories/auth_repository.dart';

class VerifyEmailOtpUseCase {
  const VerifyEmailOtpUseCase(this._repository);

  final AuthRepository _repository;

  Future<Result<bool>> call({
    required String email,
    required String challengeId,
    required String code,
  }) {
    return _repository.verifyOtp(
      email: email,
      challengeId: challengeId,
      code: code,
    );
  }
}

import '../../../../core/errors/result.dart';
import '../entities/auth_session.dart';
import '../repositories/auth_repository.dart';

class SignInWithOtpUseCase {
  const SignInWithOtpUseCase(this._repository);

  final AuthRepository _repository;

  Future<Result<AuthSession>> call({
    required String email,
    required String challengeId,
    required String code,
  }) {
    return _repository.signInWithOtp(
      email: email,
      challengeId: challengeId,
      code: code,
    );
  }
}

import '../../../../core/errors/result.dart';
import '../entities/auth_session.dart';
import '../entities/email_otp_challenge.dart';

abstract interface class AuthRepository {
  Future<Result<EmailOtpChallenge>> requestOtp(String email);

  Future<Result<bool>> verifyOtp({
    required String email,
    required String challengeId,
    required String code,
  });

  Future<Result<AuthSession>> signInWithOtp({
    required String email,
    required String challengeId,
    required String code,
  });
}

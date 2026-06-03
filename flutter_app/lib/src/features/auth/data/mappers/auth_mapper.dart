import '../../domain/entities/auth_session.dart';
import '../../domain/entities/email_otp_challenge.dart';
import '../models/auth_session_dto.dart';
import '../models/email_otp_models.dart';

extension RequestOtpResponseMapper on RequestOtpResponseDto {
  EmailOtpChallenge toEntity() {
    return EmailOtpChallenge(
      challengeId: challengeId,
      email: email,
      expiresAt: expiresAt,
    );
  }
}

extension AuthSessionMapper on AuthSessionDto {
  AuthSession toEntity() {
    return AuthSession(
      token: token,
      expiresAt: expiresAt,
      email: email,
      refreshToken: refreshToken,
    );
  }
}

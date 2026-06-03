import 'package:freezed_annotation/freezed_annotation.dart';

part 'email_otp_models.freezed.dart';
part 'email_otp_models.g.dart';

@freezed
sealed class RequestOtpDto with _$RequestOtpDto {
  const factory RequestOtpDto({
    required String email,
  }) = _RequestOtpDto;

  factory RequestOtpDto.fromJson(Map<String, dynamic> json) =>
      _$RequestOtpDtoFromJson(json);
}

@freezed
sealed class RequestOtpResponseDto with _$RequestOtpResponseDto {
  const factory RequestOtpResponseDto({
    @Default('') String challengeId,
    @Default('') String email,
    @Default('') String expiresAt,
  }) = _RequestOtpResponseDto;

  factory RequestOtpResponseDto.fromJson(Map<String, dynamic> json) =>
      _$RequestOtpResponseDtoFromJson(json);
}

@freezed
sealed class VerifyOtpDto with _$VerifyOtpDto {
  const factory VerifyOtpDto({
    required String email,
    required String challengeId,
    required String code,
  }) = _VerifyOtpDto;

  factory VerifyOtpDto.fromJson(Map<String, dynamic> json) =>
      _$VerifyOtpDtoFromJson(json);
}

@freezed
sealed class VerifyOtpResponseDto with _$VerifyOtpResponseDto {
  const factory VerifyOtpResponseDto({
    @Default(false) bool verified,
    @Default('') String challengeId,
    String? verifiedAt,
  }) = _VerifyOtpResponseDto;

  factory VerifyOtpResponseDto.fromJson(Map<String, dynamic> json) =>
      _$VerifyOtpResponseDtoFromJson(json);
}

@freezed
sealed class SignInWithOtpDto with _$SignInWithOtpDto {
  const factory SignInWithOtpDto({
    required String email,
    required String challengeId,
    required String code,
  }) = _SignInWithOtpDto;

  factory SignInWithOtpDto.fromJson(Map<String, dynamic> json) =>
      _$SignInWithOtpDtoFromJson(json);
}

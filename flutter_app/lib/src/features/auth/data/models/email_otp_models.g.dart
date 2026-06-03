// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'email_otp_models.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_RequestOtpDto _$RequestOtpDtoFromJson(Map<String, dynamic> json) =>
    _RequestOtpDto(email: json['email'] as String);

Map<String, dynamic> _$RequestOtpDtoToJson(_RequestOtpDto instance) =>
    <String, dynamic>{'email': instance.email};

_RequestOtpResponseDto _$RequestOtpResponseDtoFromJson(
  Map<String, dynamic> json,
) => _RequestOtpResponseDto(
  challengeId: json['challengeId'] as String? ?? '',
  email: json['email'] as String? ?? '',
  expiresAt: json['expiresAt'] as String? ?? '',
);

Map<String, dynamic> _$RequestOtpResponseDtoToJson(
  _RequestOtpResponseDto instance,
) => <String, dynamic>{
  'challengeId': instance.challengeId,
  'email': instance.email,
  'expiresAt': instance.expiresAt,
};

_VerifyOtpDto _$VerifyOtpDtoFromJson(Map<String, dynamic> json) =>
    _VerifyOtpDto(
      email: json['email'] as String,
      challengeId: json['challengeId'] as String,
      code: json['code'] as String,
    );

Map<String, dynamic> _$VerifyOtpDtoToJson(_VerifyOtpDto instance) =>
    <String, dynamic>{
      'email': instance.email,
      'challengeId': instance.challengeId,
      'code': instance.code,
    };

_VerifyOtpResponseDto _$VerifyOtpResponseDtoFromJson(
  Map<String, dynamic> json,
) => _VerifyOtpResponseDto(
  verified: json['verified'] as bool? ?? false,
  challengeId: json['challengeId'] as String? ?? '',
  verifiedAt: json['verifiedAt'] as String?,
);

Map<String, dynamic> _$VerifyOtpResponseDtoToJson(
  _VerifyOtpResponseDto instance,
) => <String, dynamic>{
  'verified': instance.verified,
  'challengeId': instance.challengeId,
  'verifiedAt': instance.verifiedAt,
};

_SignInWithOtpDto _$SignInWithOtpDtoFromJson(Map<String, dynamic> json) =>
    _SignInWithOtpDto(
      email: json['email'] as String,
      challengeId: json['challengeId'] as String,
      code: json['code'] as String,
    );

Map<String, dynamic> _$SignInWithOtpDtoToJson(_SignInWithOtpDto instance) =>
    <String, dynamic>{
      'email': instance.email,
      'challengeId': instance.challengeId,
      'code': instance.code,
    };

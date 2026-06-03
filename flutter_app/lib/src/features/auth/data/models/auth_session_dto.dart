import 'package:freezed_annotation/freezed_annotation.dart';

part 'auth_session_dto.freezed.dart';
part 'auth_session_dto.g.dart';

@freezed
sealed class AuthSessionDto with _$AuthSessionDto {
  const factory AuthSessionDto({
    @Default('') String token,
    @Default('') String refreshToken,
    @Default('') String expiresAt,
    @Default('') String email,
  }) = _AuthSessionDto;

  factory AuthSessionDto.fromJson(Map<String, dynamic> json) =>
      _$AuthSessionDtoFromJson(json);
}

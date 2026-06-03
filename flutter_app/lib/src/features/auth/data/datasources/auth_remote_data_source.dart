import 'package:dio/dio.dart';

import '../../../../core/network/api_response.dart';
import '../models/auth_session_dto.dart';
import '../models/email_otp_models.dart';

class AuthRemoteDataSource {
  AuthRemoteDataSource(this._dio);

  final Dio _dio;

  Future<RequestOtpResponseDto> requestOtp(RequestOtpDto request) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/auth/request-email-code/',
      data: request.toJson(),
    );

    return RequestOtpResponseDto.fromJson(response.data ?? <String, dynamic>{});
  }

  Future<VerifyOtpResponseDto> verifyOtp(VerifyOtpDto request) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/auth/verify-email-code/',
      data: request.toJson(),
    );

    return VerifyOtpResponseDto.fromJson(response.data ?? <String, dynamic>{});
  }

  Future<AuthSessionDto> signIn(SignInWithOtpDto request) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/auth/email-login/',
      data: request.toJson(),
    );

    return AuthSessionDto.fromJson(response.data ?? <String, dynamic>{});
  }

  Future<ApiResponse> logout() async {
    final response = await _dio.post<Map<String, dynamic>>('/auth/logout/');
    return ApiResponse.fromJson(response.data ?? <String, dynamic>{});
  }
}

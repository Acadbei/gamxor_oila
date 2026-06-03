import 'package:dio/dio.dart';

import '../errors/app_exception.dart';

class DioExceptionMapper {
  const DioExceptionMapper._();

  static AppException map(Object error) {
    if (error is! DioException) {
      return UnknownException(error.toString());
    }

    final statusCode = error.response?.statusCode;
    final detail = _extractDetail(error.response?.data);

    if (statusCode == 401 || statusCode == 403) {
      return UnauthorizedException(detail ?? 'Unauthorized request.');
    }
    if (statusCode == 400 || statusCode == 422) {
      return ValidationException(detail ?? 'Validation failed.');
    }
    if (statusCode != null && statusCode >= 500) {
      return ServerException(detail ?? 'Server error.');
    }
    if (error.type == DioExceptionType.connectionError ||
        error.type == DioExceptionType.connectionTimeout ||
        error.type == DioExceptionType.receiveTimeout ||
        error.type == DioExceptionType.sendTimeout) {
      return NetworkException(detail ?? 'Network request failed.');
    }

    return UnknownException(detail ?? error.message ?? 'Unknown error.');
  }

  static String? _extractDetail(Object? payload) {
    if (payload is Map<String, dynamic>) {
      final detail = payload['detail'];
      if (detail is String && detail.trim().isNotEmpty) {
        return detail.trim();
      }
    }
    return null;
  }
}

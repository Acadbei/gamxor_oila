import 'package:dio/dio.dart';

import '../../logging/app_logger.dart';

class LoggingInterceptor extends Interceptor {
  LoggingInterceptor({required AppLogger logger}) : _logger = logger;

  final AppLogger _logger;

  @override
  void onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) {
    _logger.debug(
      '[REQ] ${options.method} ${options.uri} '
      'query=${options.queryParameters}',
    );
    super.onRequest(options, handler);
  }

  @override
  void onResponse(Response response, ResponseInterceptorHandler handler) {
    _logger.info(
      '[RES] ${response.requestOptions.method} ${response.requestOptions.uri} '
      'status=${response.statusCode}',
    );
    super.onResponse(response, handler);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    _logger.error(
      '[ERR] ${err.requestOptions.method} ${err.requestOptions.uri}',
      err,
      err.stackTrace,
    );
    super.onError(err, handler);
  }
}

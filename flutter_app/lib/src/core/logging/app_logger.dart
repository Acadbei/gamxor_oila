import 'package:logger/logger.dart';

class AppLogger {
  AppLogger._() : _logger = Logger();

  static final AppLogger instance = AppLogger._();

  final Logger _logger;

  void debug(String message) => _logger.d(message);

  void info(String message) => _logger.i(message);

  void warning(String message) => _logger.w(message);

  void error(String message, [Object? error, StackTrace? stackTrace]) {
    _logger.e(message, error: error, stackTrace: stackTrace);
  }
}

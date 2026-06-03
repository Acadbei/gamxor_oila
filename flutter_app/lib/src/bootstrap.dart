import 'dart:async';

import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app/app.dart';
import 'core/logging/app_logger.dart';

Future<void> bootstrap() async {
  WidgetsFlutterBinding.ensureInitialized();

  final logger = AppLogger.instance;

  FlutterError.onError = (details) {
    logger.error('Unhandled Flutter error', details.exception, details.stack);
  };

  runZonedGuarded(
    () => runApp(const ProviderScope(child: FamilyCareApp())),
    (error, stackTrace) => logger.error(
      'Unhandled zone error',
      error,
      stackTrace,
    ),
  );
}

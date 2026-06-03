import 'package:flutter/material.dart';

class AppTheme {
  const AppTheme._();

  static final ThemeData light = ThemeData(
    colorScheme: ColorScheme.fromSeed(
      seedColor: const Color(0xFF0C8A73),
      brightness: Brightness.light,
    ),
    useMaterial3: true,
    scaffoldBackgroundColor: const Color(0xFFF5F7F4),
  );

  static final ThemeData dark = ThemeData(
    colorScheme: ColorScheme.fromSeed(
      seedColor: const Color(0xFF0C8A73),
      brightness: Brightness.dark,
    ),
    useMaterial3: true,
  );
}

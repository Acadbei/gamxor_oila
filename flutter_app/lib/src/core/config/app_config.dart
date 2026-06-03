class AppConfig {
  const AppConfig._();

  static const String appName = 'Family Care';
  static const String androidApplicationId = 'com.familycare.app';
  static const String iosBundleId = 'com.familycare.ios';
  static const String apiBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8000/api/v1',
  );
  static const Duration connectTimeout = Duration(seconds: 20);
  static const Duration receiveTimeout = Duration(seconds: 20);
}

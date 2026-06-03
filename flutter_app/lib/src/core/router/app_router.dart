import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/auth/presentation/pages/sign_in_page.dart';
import '../../features/auth/presentation/pages/verify_otp_page.dart';
import '../../features/splash/presentation/pages/splash_page.dart';
import '../constants/route_paths.dart';

final _rootNavigatorKey = GlobalKey<NavigatorState>();

final appRouterProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    navigatorKey: _rootNavigatorKey,
    initialLocation: RoutePaths.splash,
    routes: [
      GoRoute(
        path: RoutePaths.splash,
        builder: (context, state) => const SplashPage(),
      ),
      GoRoute(
        path: RoutePaths.signIn,
        builder: (context, state) => const SignInPage(),
      ),
      GoRoute(
        path: RoutePaths.verifyOtp,
        builder: (context, state) => const VerifyOtpPage(),
      ),
    ],
  );
});

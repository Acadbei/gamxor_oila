import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/di/providers.dart';
import '../../domain/entities/email_otp_challenge.dart';

final authControllerProvider =
    AutoDisposeAsyncNotifierProvider<AuthController, EmailOtpChallenge?>(
      AuthController.new,
    );

class AuthController extends AutoDisposeAsyncNotifier<EmailOtpChallenge?> {
  @override
  Future<EmailOtpChallenge?> build() async => null;

  Future<void> requestOtp(String email) async {
    state = const AsyncLoading();

    final useCase = ref.read(requestEmailOtpUseCaseProvider);
    final result = await useCase(email);

    state = result.when(
      success: AsyncData.new,
      failure: (failure) => AsyncError(
        failure,
        StackTrace.current,
      ),
    );
  }
}

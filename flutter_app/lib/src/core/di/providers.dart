import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../features/auth/data/datasources/auth_remote_data_source.dart';
import '../../features/auth/data/repositories/auth_repository_impl.dart';
import '../../features/auth/domain/repositories/auth_repository.dart';
import '../../features/auth/domain/usecases/request_email_otp_use_case.dart';
import '../../features/auth/domain/usecases/sign_in_with_otp_use_case.dart';
import '../../features/auth/domain/usecases/verify_email_otp_use_case.dart';
import '../network/dio_client.dart';

final authRemoteDataSourceProvider = Provider<AuthRemoteDataSource>((ref) {
  return AuthRemoteDataSource(ref.watch(dioProvider));
});

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return AuthRepositoryImpl(ref.watch(authRemoteDataSourceProvider));
});

final requestEmailOtpUseCaseProvider = Provider<RequestEmailOtpUseCase>((ref) {
  return RequestEmailOtpUseCase(ref.watch(authRepositoryProvider));
});

final verifyEmailOtpUseCaseProvider = Provider<VerifyEmailOtpUseCase>((ref) {
  return VerifyEmailOtpUseCase(ref.watch(authRepositoryProvider));
});

final signInWithOtpUseCaseProvider = Provider<SignInWithOtpUseCase>((ref) {
  return SignInWithOtpUseCase(ref.watch(authRepositoryProvider));
});

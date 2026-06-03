import '../../../../core/errors/app_exception.dart';
import '../../../../core/errors/failure.dart';
import '../../../../core/errors/result.dart';
import '../../../../core/network/dio_exception_mapper.dart';
import '../../domain/entities/auth_session.dart';
import '../../domain/entities/email_otp_challenge.dart';
import '../../domain/repositories/auth_repository.dart';
import '../datasources/auth_remote_data_source.dart';
import '../mappers/auth_mapper.dart';
import '../models/email_otp_models.dart';

class AuthRepositoryImpl implements AuthRepository {
  AuthRepositoryImpl(this._remoteDataSource);

  final AuthRemoteDataSource _remoteDataSource;

  @override
  Future<Result<EmailOtpChallenge>> requestOtp(String email) async {
    try {
      final response = await _remoteDataSource.requestOtp(
        RequestOtpDto(email: email),
      );
      return Success(response.toEntity());
    } on AppException catch (error) {
      return Error(_mapFailure(error));
    } catch (error) {
      return Error(_mapFailure(DioExceptionMapper.map(error)));
    }
  }

  @override
  Future<Result<bool>> verifyOtp({
    required String email,
    required String challengeId,
    required String code,
  }) async {
    try {
      final response = await _remoteDataSource.verifyOtp(
        VerifyOtpDto(
          email: email,
          challengeId: challengeId,
          code: code,
        ),
      );

      if (response.verified) {
        return const Success(true);
      }

      return const Error(ValidationFailure('OTP verification failed.'));
    } on AppException catch (error) {
      return Error(_mapFailure(error));
    } catch (error) {
      return Error(_mapFailure(DioExceptionMapper.map(error)));
    }
  }

  @override
  Future<Result<AuthSession>> signInWithOtp({
    required String email,
    required String challengeId,
    required String code,
  }) async {
    try {
      final response = await _remoteDataSource.signIn(
        SignInWithOtpDto(
          email: email,
          challengeId: challengeId,
          code: code,
        ),
      );
      return Success(response.toEntity());
    } on AppException catch (error) {
      return Error(_mapFailure(error));
    } catch (error) {
      return Error(_mapFailure(DioExceptionMapper.map(error)));
    }
  }

  Failure _mapFailure(AppException exception) {
    return switch (exception) {
      NetworkException(message: final message) => NetworkFailure(message),
      ValidationException(message: final message) => ValidationFailure(message),
      UnauthorizedException(message: final message) =>
        UnauthorizedFailure(message),
      ServerException(message: final message) => ServerFailure(message),
      UnknownException(message: final message) => UnknownFailure(message),
    };
  }
}

class AuthSession {
  const AuthSession({
    required this.token,
    required this.refreshToken,
    required this.expiresAt,
    required this.email,
  });

  final String token;
  final String refreshToken;
  final String expiresAt;
  final String email;
}

class EmailOtpChallenge {
  const EmailOtpChallenge({
    required this.challengeId,
    required this.email,
    required this.expiresAt,
  });

  final String challengeId;
  final String email;
  final String expiresAt;
}

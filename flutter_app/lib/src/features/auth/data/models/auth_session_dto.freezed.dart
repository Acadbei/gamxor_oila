// GENERATED CODE - DO NOT MODIFY BY HAND
// coverage:ignore-file
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'auth_session_dto.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$AuthSessionDto {

 String get token; String get refreshToken; String get expiresAt; String get email;
/// Create a copy of AuthSessionDto
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$AuthSessionDtoCopyWith<AuthSessionDto> get copyWith => _$AuthSessionDtoCopyWithImpl<AuthSessionDto>(this as AuthSessionDto, _$identity);

  /// Serializes this AuthSessionDto to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is AuthSessionDto&&(identical(other.token, token) || other.token == token)&&(identical(other.refreshToken, refreshToken) || other.refreshToken == refreshToken)&&(identical(other.expiresAt, expiresAt) || other.expiresAt == expiresAt)&&(identical(other.email, email) || other.email == email));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,token,refreshToken,expiresAt,email);

@override
String toString() {
  return 'AuthSessionDto(token: $token, refreshToken: $refreshToken, expiresAt: $expiresAt, email: $email)';
}


}

/// @nodoc
abstract mixin class $AuthSessionDtoCopyWith<$Res>  {
  factory $AuthSessionDtoCopyWith(AuthSessionDto value, $Res Function(AuthSessionDto) _then) = _$AuthSessionDtoCopyWithImpl;
@useResult
$Res call({
 String token, String refreshToken, String expiresAt, String email
});




}
/// @nodoc
class _$AuthSessionDtoCopyWithImpl<$Res>
    implements $AuthSessionDtoCopyWith<$Res> {
  _$AuthSessionDtoCopyWithImpl(this._self, this._then);

  final AuthSessionDto _self;
  final $Res Function(AuthSessionDto) _then;

/// Create a copy of AuthSessionDto
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? token = null,Object? refreshToken = null,Object? expiresAt = null,Object? email = null,}) {
  return _then(_self.copyWith(
token: null == token ? _self.token : token // ignore: cast_nullable_to_non_nullable
as String,refreshToken: null == refreshToken ? _self.refreshToken : refreshToken // ignore: cast_nullable_to_non_nullable
as String,expiresAt: null == expiresAt ? _self.expiresAt : expiresAt // ignore: cast_nullable_to_non_nullable
as String,email: null == email ? _self.email : email // ignore: cast_nullable_to_non_nullable
as String,
  ));
}

}


/// Adds pattern-matching-related methods to [AuthSessionDto].
extension AuthSessionDtoPatterns on AuthSessionDto {
/// A variant of `map` that fallback to returning `orElse`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _AuthSessionDto value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _AuthSessionDto() when $default != null:
return $default(_that);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// Callbacks receives the raw object, upcasted.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case final Subclass2 value:
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _AuthSessionDto value)  $default,){
final _that = this;
switch (_that) {
case _AuthSessionDto():
return $default(_that);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `map` that fallback to returning `null`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _AuthSessionDto value)?  $default,){
final _that = this;
switch (_that) {
case _AuthSessionDto() when $default != null:
return $default(_that);case _:
  return null;

}
}
/// A variant of `when` that fallback to an `orElse` callback.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String token,  String refreshToken,  String expiresAt,  String email)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _AuthSessionDto() when $default != null:
return $default(_that.token,_that.refreshToken,_that.expiresAt,_that.email);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// As opposed to `map`, this offers destructuring.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case Subclass2(:final field2):
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String token,  String refreshToken,  String expiresAt,  String email)  $default,) {final _that = this;
switch (_that) {
case _AuthSessionDto():
return $default(_that.token,_that.refreshToken,_that.expiresAt,_that.email);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `when` that fallback to returning `null`
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String token,  String refreshToken,  String expiresAt,  String email)?  $default,) {final _that = this;
switch (_that) {
case _AuthSessionDto() when $default != null:
return $default(_that.token,_that.refreshToken,_that.expiresAt,_that.email);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _AuthSessionDto implements AuthSessionDto {
  const _AuthSessionDto({this.token = '', this.refreshToken = '', this.expiresAt = '', this.email = ''});
  factory _AuthSessionDto.fromJson(Map<String, dynamic> json) => _$AuthSessionDtoFromJson(json);

@override@JsonKey() final  String token;
@override@JsonKey() final  String refreshToken;
@override@JsonKey() final  String expiresAt;
@override@JsonKey() final  String email;

/// Create a copy of AuthSessionDto
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$AuthSessionDtoCopyWith<_AuthSessionDto> get copyWith => __$AuthSessionDtoCopyWithImpl<_AuthSessionDto>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$AuthSessionDtoToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _AuthSessionDto&&(identical(other.token, token) || other.token == token)&&(identical(other.refreshToken, refreshToken) || other.refreshToken == refreshToken)&&(identical(other.expiresAt, expiresAt) || other.expiresAt == expiresAt)&&(identical(other.email, email) || other.email == email));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,token,refreshToken,expiresAt,email);

@override
String toString() {
  return 'AuthSessionDto(token: $token, refreshToken: $refreshToken, expiresAt: $expiresAt, email: $email)';
}


}

/// @nodoc
abstract mixin class _$AuthSessionDtoCopyWith<$Res> implements $AuthSessionDtoCopyWith<$Res> {
  factory _$AuthSessionDtoCopyWith(_AuthSessionDto value, $Res Function(_AuthSessionDto) _then) = __$AuthSessionDtoCopyWithImpl;
@override @useResult
$Res call({
 String token, String refreshToken, String expiresAt, String email
});




}
/// @nodoc
class __$AuthSessionDtoCopyWithImpl<$Res>
    implements _$AuthSessionDtoCopyWith<$Res> {
  __$AuthSessionDtoCopyWithImpl(this._self, this._then);

  final _AuthSessionDto _self;
  final $Res Function(_AuthSessionDto) _then;

/// Create a copy of AuthSessionDto
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? token = null,Object? refreshToken = null,Object? expiresAt = null,Object? email = null,}) {
  return _then(_AuthSessionDto(
token: null == token ? _self.token : token // ignore: cast_nullable_to_non_nullable
as String,refreshToken: null == refreshToken ? _self.refreshToken : refreshToken // ignore: cast_nullable_to_non_nullable
as String,expiresAt: null == expiresAt ? _self.expiresAt : expiresAt // ignore: cast_nullable_to_non_nullable
as String,email: null == email ? _self.email : email // ignore: cast_nullable_to_non_nullable
as String,
  ));
}


}

// dart format on

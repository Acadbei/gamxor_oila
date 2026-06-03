// GENERATED CODE - DO NOT MODIFY BY HAND
// coverage:ignore-file
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'email_otp_models.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$RequestOtpDto {

 String get email;
/// Create a copy of RequestOtpDto
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$RequestOtpDtoCopyWith<RequestOtpDto> get copyWith => _$RequestOtpDtoCopyWithImpl<RequestOtpDto>(this as RequestOtpDto, _$identity);

  /// Serializes this RequestOtpDto to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is RequestOtpDto&&(identical(other.email, email) || other.email == email));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,email);

@override
String toString() {
  return 'RequestOtpDto(email: $email)';
}


}

/// @nodoc
abstract mixin class $RequestOtpDtoCopyWith<$Res>  {
  factory $RequestOtpDtoCopyWith(RequestOtpDto value, $Res Function(RequestOtpDto) _then) = _$RequestOtpDtoCopyWithImpl;
@useResult
$Res call({
 String email
});




}
/// @nodoc
class _$RequestOtpDtoCopyWithImpl<$Res>
    implements $RequestOtpDtoCopyWith<$Res> {
  _$RequestOtpDtoCopyWithImpl(this._self, this._then);

  final RequestOtpDto _self;
  final $Res Function(RequestOtpDto) _then;

/// Create a copy of RequestOtpDto
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? email = null,}) {
  return _then(_self.copyWith(
email: null == email ? _self.email : email // ignore: cast_nullable_to_non_nullable
as String,
  ));
}

}


/// Adds pattern-matching-related methods to [RequestOtpDto].
extension RequestOtpDtoPatterns on RequestOtpDto {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _RequestOtpDto value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _RequestOtpDto() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _RequestOtpDto value)  $default,){
final _that = this;
switch (_that) {
case _RequestOtpDto():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _RequestOtpDto value)?  $default,){
final _that = this;
switch (_that) {
case _RequestOtpDto() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String email)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _RequestOtpDto() when $default != null:
return $default(_that.email);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String email)  $default,) {final _that = this;
switch (_that) {
case _RequestOtpDto():
return $default(_that.email);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String email)?  $default,) {final _that = this;
switch (_that) {
case _RequestOtpDto() when $default != null:
return $default(_that.email);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _RequestOtpDto implements RequestOtpDto {
  const _RequestOtpDto({required this.email});
  factory _RequestOtpDto.fromJson(Map<String, dynamic> json) => _$RequestOtpDtoFromJson(json);

@override final  String email;

/// Create a copy of RequestOtpDto
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$RequestOtpDtoCopyWith<_RequestOtpDto> get copyWith => __$RequestOtpDtoCopyWithImpl<_RequestOtpDto>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$RequestOtpDtoToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _RequestOtpDto&&(identical(other.email, email) || other.email == email));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,email);

@override
String toString() {
  return 'RequestOtpDto(email: $email)';
}


}

/// @nodoc
abstract mixin class _$RequestOtpDtoCopyWith<$Res> implements $RequestOtpDtoCopyWith<$Res> {
  factory _$RequestOtpDtoCopyWith(_RequestOtpDto value, $Res Function(_RequestOtpDto) _then) = __$RequestOtpDtoCopyWithImpl;
@override @useResult
$Res call({
 String email
});




}
/// @nodoc
class __$RequestOtpDtoCopyWithImpl<$Res>
    implements _$RequestOtpDtoCopyWith<$Res> {
  __$RequestOtpDtoCopyWithImpl(this._self, this._then);

  final _RequestOtpDto _self;
  final $Res Function(_RequestOtpDto) _then;

/// Create a copy of RequestOtpDto
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? email = null,}) {
  return _then(_RequestOtpDto(
email: null == email ? _self.email : email // ignore: cast_nullable_to_non_nullable
as String,
  ));
}


}


/// @nodoc
mixin _$RequestOtpResponseDto {

 String get challengeId; String get email; String get expiresAt;
/// Create a copy of RequestOtpResponseDto
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$RequestOtpResponseDtoCopyWith<RequestOtpResponseDto> get copyWith => _$RequestOtpResponseDtoCopyWithImpl<RequestOtpResponseDto>(this as RequestOtpResponseDto, _$identity);

  /// Serializes this RequestOtpResponseDto to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is RequestOtpResponseDto&&(identical(other.challengeId, challengeId) || other.challengeId == challengeId)&&(identical(other.email, email) || other.email == email)&&(identical(other.expiresAt, expiresAt) || other.expiresAt == expiresAt));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,challengeId,email,expiresAt);

@override
String toString() {
  return 'RequestOtpResponseDto(challengeId: $challengeId, email: $email, expiresAt: $expiresAt)';
}


}

/// @nodoc
abstract mixin class $RequestOtpResponseDtoCopyWith<$Res>  {
  factory $RequestOtpResponseDtoCopyWith(RequestOtpResponseDto value, $Res Function(RequestOtpResponseDto) _then) = _$RequestOtpResponseDtoCopyWithImpl;
@useResult
$Res call({
 String challengeId, String email, String expiresAt
});




}
/// @nodoc
class _$RequestOtpResponseDtoCopyWithImpl<$Res>
    implements $RequestOtpResponseDtoCopyWith<$Res> {
  _$RequestOtpResponseDtoCopyWithImpl(this._self, this._then);

  final RequestOtpResponseDto _self;
  final $Res Function(RequestOtpResponseDto) _then;

/// Create a copy of RequestOtpResponseDto
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? challengeId = null,Object? email = null,Object? expiresAt = null,}) {
  return _then(_self.copyWith(
challengeId: null == challengeId ? _self.challengeId : challengeId // ignore: cast_nullable_to_non_nullable
as String,email: null == email ? _self.email : email // ignore: cast_nullable_to_non_nullable
as String,expiresAt: null == expiresAt ? _self.expiresAt : expiresAt // ignore: cast_nullable_to_non_nullable
as String,
  ));
}

}


/// Adds pattern-matching-related methods to [RequestOtpResponseDto].
extension RequestOtpResponseDtoPatterns on RequestOtpResponseDto {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _RequestOtpResponseDto value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _RequestOtpResponseDto() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _RequestOtpResponseDto value)  $default,){
final _that = this;
switch (_that) {
case _RequestOtpResponseDto():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _RequestOtpResponseDto value)?  $default,){
final _that = this;
switch (_that) {
case _RequestOtpResponseDto() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String challengeId,  String email,  String expiresAt)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _RequestOtpResponseDto() when $default != null:
return $default(_that.challengeId,_that.email,_that.expiresAt);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String challengeId,  String email,  String expiresAt)  $default,) {final _that = this;
switch (_that) {
case _RequestOtpResponseDto():
return $default(_that.challengeId,_that.email,_that.expiresAt);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String challengeId,  String email,  String expiresAt)?  $default,) {final _that = this;
switch (_that) {
case _RequestOtpResponseDto() when $default != null:
return $default(_that.challengeId,_that.email,_that.expiresAt);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _RequestOtpResponseDto implements RequestOtpResponseDto {
  const _RequestOtpResponseDto({this.challengeId = '', this.email = '', this.expiresAt = ''});
  factory _RequestOtpResponseDto.fromJson(Map<String, dynamic> json) => _$RequestOtpResponseDtoFromJson(json);

@override@JsonKey() final  String challengeId;
@override@JsonKey() final  String email;
@override@JsonKey() final  String expiresAt;

/// Create a copy of RequestOtpResponseDto
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$RequestOtpResponseDtoCopyWith<_RequestOtpResponseDto> get copyWith => __$RequestOtpResponseDtoCopyWithImpl<_RequestOtpResponseDto>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$RequestOtpResponseDtoToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _RequestOtpResponseDto&&(identical(other.challengeId, challengeId) || other.challengeId == challengeId)&&(identical(other.email, email) || other.email == email)&&(identical(other.expiresAt, expiresAt) || other.expiresAt == expiresAt));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,challengeId,email,expiresAt);

@override
String toString() {
  return 'RequestOtpResponseDto(challengeId: $challengeId, email: $email, expiresAt: $expiresAt)';
}


}

/// @nodoc
abstract mixin class _$RequestOtpResponseDtoCopyWith<$Res> implements $RequestOtpResponseDtoCopyWith<$Res> {
  factory _$RequestOtpResponseDtoCopyWith(_RequestOtpResponseDto value, $Res Function(_RequestOtpResponseDto) _then) = __$RequestOtpResponseDtoCopyWithImpl;
@override @useResult
$Res call({
 String challengeId, String email, String expiresAt
});




}
/// @nodoc
class __$RequestOtpResponseDtoCopyWithImpl<$Res>
    implements _$RequestOtpResponseDtoCopyWith<$Res> {
  __$RequestOtpResponseDtoCopyWithImpl(this._self, this._then);

  final _RequestOtpResponseDto _self;
  final $Res Function(_RequestOtpResponseDto) _then;

/// Create a copy of RequestOtpResponseDto
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? challengeId = null,Object? email = null,Object? expiresAt = null,}) {
  return _then(_RequestOtpResponseDto(
challengeId: null == challengeId ? _self.challengeId : challengeId // ignore: cast_nullable_to_non_nullable
as String,email: null == email ? _self.email : email // ignore: cast_nullable_to_non_nullable
as String,expiresAt: null == expiresAt ? _self.expiresAt : expiresAt // ignore: cast_nullable_to_non_nullable
as String,
  ));
}


}


/// @nodoc
mixin _$VerifyOtpDto {

 String get email; String get challengeId; String get code;
/// Create a copy of VerifyOtpDto
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$VerifyOtpDtoCopyWith<VerifyOtpDto> get copyWith => _$VerifyOtpDtoCopyWithImpl<VerifyOtpDto>(this as VerifyOtpDto, _$identity);

  /// Serializes this VerifyOtpDto to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is VerifyOtpDto&&(identical(other.email, email) || other.email == email)&&(identical(other.challengeId, challengeId) || other.challengeId == challengeId)&&(identical(other.code, code) || other.code == code));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,email,challengeId,code);

@override
String toString() {
  return 'VerifyOtpDto(email: $email, challengeId: $challengeId, code: $code)';
}


}

/// @nodoc
abstract mixin class $VerifyOtpDtoCopyWith<$Res>  {
  factory $VerifyOtpDtoCopyWith(VerifyOtpDto value, $Res Function(VerifyOtpDto) _then) = _$VerifyOtpDtoCopyWithImpl;
@useResult
$Res call({
 String email, String challengeId, String code
});




}
/// @nodoc
class _$VerifyOtpDtoCopyWithImpl<$Res>
    implements $VerifyOtpDtoCopyWith<$Res> {
  _$VerifyOtpDtoCopyWithImpl(this._self, this._then);

  final VerifyOtpDto _self;
  final $Res Function(VerifyOtpDto) _then;

/// Create a copy of VerifyOtpDto
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? email = null,Object? challengeId = null,Object? code = null,}) {
  return _then(_self.copyWith(
email: null == email ? _self.email : email // ignore: cast_nullable_to_non_nullable
as String,challengeId: null == challengeId ? _self.challengeId : challengeId // ignore: cast_nullable_to_non_nullable
as String,code: null == code ? _self.code : code // ignore: cast_nullable_to_non_nullable
as String,
  ));
}

}


/// Adds pattern-matching-related methods to [VerifyOtpDto].
extension VerifyOtpDtoPatterns on VerifyOtpDto {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _VerifyOtpDto value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _VerifyOtpDto() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _VerifyOtpDto value)  $default,){
final _that = this;
switch (_that) {
case _VerifyOtpDto():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _VerifyOtpDto value)?  $default,){
final _that = this;
switch (_that) {
case _VerifyOtpDto() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String email,  String challengeId,  String code)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _VerifyOtpDto() when $default != null:
return $default(_that.email,_that.challengeId,_that.code);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String email,  String challengeId,  String code)  $default,) {final _that = this;
switch (_that) {
case _VerifyOtpDto():
return $default(_that.email,_that.challengeId,_that.code);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String email,  String challengeId,  String code)?  $default,) {final _that = this;
switch (_that) {
case _VerifyOtpDto() when $default != null:
return $default(_that.email,_that.challengeId,_that.code);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _VerifyOtpDto implements VerifyOtpDto {
  const _VerifyOtpDto({required this.email, required this.challengeId, required this.code});
  factory _VerifyOtpDto.fromJson(Map<String, dynamic> json) => _$VerifyOtpDtoFromJson(json);

@override final  String email;
@override final  String challengeId;
@override final  String code;

/// Create a copy of VerifyOtpDto
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$VerifyOtpDtoCopyWith<_VerifyOtpDto> get copyWith => __$VerifyOtpDtoCopyWithImpl<_VerifyOtpDto>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$VerifyOtpDtoToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _VerifyOtpDto&&(identical(other.email, email) || other.email == email)&&(identical(other.challengeId, challengeId) || other.challengeId == challengeId)&&(identical(other.code, code) || other.code == code));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,email,challengeId,code);

@override
String toString() {
  return 'VerifyOtpDto(email: $email, challengeId: $challengeId, code: $code)';
}


}

/// @nodoc
abstract mixin class _$VerifyOtpDtoCopyWith<$Res> implements $VerifyOtpDtoCopyWith<$Res> {
  factory _$VerifyOtpDtoCopyWith(_VerifyOtpDto value, $Res Function(_VerifyOtpDto) _then) = __$VerifyOtpDtoCopyWithImpl;
@override @useResult
$Res call({
 String email, String challengeId, String code
});




}
/// @nodoc
class __$VerifyOtpDtoCopyWithImpl<$Res>
    implements _$VerifyOtpDtoCopyWith<$Res> {
  __$VerifyOtpDtoCopyWithImpl(this._self, this._then);

  final _VerifyOtpDto _self;
  final $Res Function(_VerifyOtpDto) _then;

/// Create a copy of VerifyOtpDto
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? email = null,Object? challengeId = null,Object? code = null,}) {
  return _then(_VerifyOtpDto(
email: null == email ? _self.email : email // ignore: cast_nullable_to_non_nullable
as String,challengeId: null == challengeId ? _self.challengeId : challengeId // ignore: cast_nullable_to_non_nullable
as String,code: null == code ? _self.code : code // ignore: cast_nullable_to_non_nullable
as String,
  ));
}


}


/// @nodoc
mixin _$VerifyOtpResponseDto {

 bool get verified; String get challengeId; String? get verifiedAt;
/// Create a copy of VerifyOtpResponseDto
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$VerifyOtpResponseDtoCopyWith<VerifyOtpResponseDto> get copyWith => _$VerifyOtpResponseDtoCopyWithImpl<VerifyOtpResponseDto>(this as VerifyOtpResponseDto, _$identity);

  /// Serializes this VerifyOtpResponseDto to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is VerifyOtpResponseDto&&(identical(other.verified, verified) || other.verified == verified)&&(identical(other.challengeId, challengeId) || other.challengeId == challengeId)&&(identical(other.verifiedAt, verifiedAt) || other.verifiedAt == verifiedAt));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,verified,challengeId,verifiedAt);

@override
String toString() {
  return 'VerifyOtpResponseDto(verified: $verified, challengeId: $challengeId, verifiedAt: $verifiedAt)';
}


}

/// @nodoc
abstract mixin class $VerifyOtpResponseDtoCopyWith<$Res>  {
  factory $VerifyOtpResponseDtoCopyWith(VerifyOtpResponseDto value, $Res Function(VerifyOtpResponseDto) _then) = _$VerifyOtpResponseDtoCopyWithImpl;
@useResult
$Res call({
 bool verified, String challengeId, String? verifiedAt
});




}
/// @nodoc
class _$VerifyOtpResponseDtoCopyWithImpl<$Res>
    implements $VerifyOtpResponseDtoCopyWith<$Res> {
  _$VerifyOtpResponseDtoCopyWithImpl(this._self, this._then);

  final VerifyOtpResponseDto _self;
  final $Res Function(VerifyOtpResponseDto) _then;

/// Create a copy of VerifyOtpResponseDto
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? verified = null,Object? challengeId = null,Object? verifiedAt = freezed,}) {
  return _then(_self.copyWith(
verified: null == verified ? _self.verified : verified // ignore: cast_nullable_to_non_nullable
as bool,challengeId: null == challengeId ? _self.challengeId : challengeId // ignore: cast_nullable_to_non_nullable
as String,verifiedAt: freezed == verifiedAt ? _self.verifiedAt : verifiedAt // ignore: cast_nullable_to_non_nullable
as String?,
  ));
}

}


/// Adds pattern-matching-related methods to [VerifyOtpResponseDto].
extension VerifyOtpResponseDtoPatterns on VerifyOtpResponseDto {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _VerifyOtpResponseDto value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _VerifyOtpResponseDto() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _VerifyOtpResponseDto value)  $default,){
final _that = this;
switch (_that) {
case _VerifyOtpResponseDto():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _VerifyOtpResponseDto value)?  $default,){
final _that = this;
switch (_that) {
case _VerifyOtpResponseDto() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( bool verified,  String challengeId,  String? verifiedAt)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _VerifyOtpResponseDto() when $default != null:
return $default(_that.verified,_that.challengeId,_that.verifiedAt);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( bool verified,  String challengeId,  String? verifiedAt)  $default,) {final _that = this;
switch (_that) {
case _VerifyOtpResponseDto():
return $default(_that.verified,_that.challengeId,_that.verifiedAt);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( bool verified,  String challengeId,  String? verifiedAt)?  $default,) {final _that = this;
switch (_that) {
case _VerifyOtpResponseDto() when $default != null:
return $default(_that.verified,_that.challengeId,_that.verifiedAt);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _VerifyOtpResponseDto implements VerifyOtpResponseDto {
  const _VerifyOtpResponseDto({this.verified = false, this.challengeId = '', this.verifiedAt});
  factory _VerifyOtpResponseDto.fromJson(Map<String, dynamic> json) => _$VerifyOtpResponseDtoFromJson(json);

@override@JsonKey() final  bool verified;
@override@JsonKey() final  String challengeId;
@override final  String? verifiedAt;

/// Create a copy of VerifyOtpResponseDto
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$VerifyOtpResponseDtoCopyWith<_VerifyOtpResponseDto> get copyWith => __$VerifyOtpResponseDtoCopyWithImpl<_VerifyOtpResponseDto>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$VerifyOtpResponseDtoToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _VerifyOtpResponseDto&&(identical(other.verified, verified) || other.verified == verified)&&(identical(other.challengeId, challengeId) || other.challengeId == challengeId)&&(identical(other.verifiedAt, verifiedAt) || other.verifiedAt == verifiedAt));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,verified,challengeId,verifiedAt);

@override
String toString() {
  return 'VerifyOtpResponseDto(verified: $verified, challengeId: $challengeId, verifiedAt: $verifiedAt)';
}


}

/// @nodoc
abstract mixin class _$VerifyOtpResponseDtoCopyWith<$Res> implements $VerifyOtpResponseDtoCopyWith<$Res> {
  factory _$VerifyOtpResponseDtoCopyWith(_VerifyOtpResponseDto value, $Res Function(_VerifyOtpResponseDto) _then) = __$VerifyOtpResponseDtoCopyWithImpl;
@override @useResult
$Res call({
 bool verified, String challengeId, String? verifiedAt
});




}
/// @nodoc
class __$VerifyOtpResponseDtoCopyWithImpl<$Res>
    implements _$VerifyOtpResponseDtoCopyWith<$Res> {
  __$VerifyOtpResponseDtoCopyWithImpl(this._self, this._then);

  final _VerifyOtpResponseDto _self;
  final $Res Function(_VerifyOtpResponseDto) _then;

/// Create a copy of VerifyOtpResponseDto
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? verified = null,Object? challengeId = null,Object? verifiedAt = freezed,}) {
  return _then(_VerifyOtpResponseDto(
verified: null == verified ? _self.verified : verified // ignore: cast_nullable_to_non_nullable
as bool,challengeId: null == challengeId ? _self.challengeId : challengeId // ignore: cast_nullable_to_non_nullable
as String,verifiedAt: freezed == verifiedAt ? _self.verifiedAt : verifiedAt // ignore: cast_nullable_to_non_nullable
as String?,
  ));
}


}


/// @nodoc
mixin _$SignInWithOtpDto {

 String get email; String get challengeId; String get code;
/// Create a copy of SignInWithOtpDto
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$SignInWithOtpDtoCopyWith<SignInWithOtpDto> get copyWith => _$SignInWithOtpDtoCopyWithImpl<SignInWithOtpDto>(this as SignInWithOtpDto, _$identity);

  /// Serializes this SignInWithOtpDto to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is SignInWithOtpDto&&(identical(other.email, email) || other.email == email)&&(identical(other.challengeId, challengeId) || other.challengeId == challengeId)&&(identical(other.code, code) || other.code == code));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,email,challengeId,code);

@override
String toString() {
  return 'SignInWithOtpDto(email: $email, challengeId: $challengeId, code: $code)';
}


}

/// @nodoc
abstract mixin class $SignInWithOtpDtoCopyWith<$Res>  {
  factory $SignInWithOtpDtoCopyWith(SignInWithOtpDto value, $Res Function(SignInWithOtpDto) _then) = _$SignInWithOtpDtoCopyWithImpl;
@useResult
$Res call({
 String email, String challengeId, String code
});




}
/// @nodoc
class _$SignInWithOtpDtoCopyWithImpl<$Res>
    implements $SignInWithOtpDtoCopyWith<$Res> {
  _$SignInWithOtpDtoCopyWithImpl(this._self, this._then);

  final SignInWithOtpDto _self;
  final $Res Function(SignInWithOtpDto) _then;

/// Create a copy of SignInWithOtpDto
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? email = null,Object? challengeId = null,Object? code = null,}) {
  return _then(_self.copyWith(
email: null == email ? _self.email : email // ignore: cast_nullable_to_non_nullable
as String,challengeId: null == challengeId ? _self.challengeId : challengeId // ignore: cast_nullable_to_non_nullable
as String,code: null == code ? _self.code : code // ignore: cast_nullable_to_non_nullable
as String,
  ));
}

}


/// Adds pattern-matching-related methods to [SignInWithOtpDto].
extension SignInWithOtpDtoPatterns on SignInWithOtpDto {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _SignInWithOtpDto value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _SignInWithOtpDto() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _SignInWithOtpDto value)  $default,){
final _that = this;
switch (_that) {
case _SignInWithOtpDto():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _SignInWithOtpDto value)?  $default,){
final _that = this;
switch (_that) {
case _SignInWithOtpDto() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String email,  String challengeId,  String code)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _SignInWithOtpDto() when $default != null:
return $default(_that.email,_that.challengeId,_that.code);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String email,  String challengeId,  String code)  $default,) {final _that = this;
switch (_that) {
case _SignInWithOtpDto():
return $default(_that.email,_that.challengeId,_that.code);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String email,  String challengeId,  String code)?  $default,) {final _that = this;
switch (_that) {
case _SignInWithOtpDto() when $default != null:
return $default(_that.email,_that.challengeId,_that.code);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _SignInWithOtpDto implements SignInWithOtpDto {
  const _SignInWithOtpDto({required this.email, required this.challengeId, required this.code});
  factory _SignInWithOtpDto.fromJson(Map<String, dynamic> json) => _$SignInWithOtpDtoFromJson(json);

@override final  String email;
@override final  String challengeId;
@override final  String code;

/// Create a copy of SignInWithOtpDto
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$SignInWithOtpDtoCopyWith<_SignInWithOtpDto> get copyWith => __$SignInWithOtpDtoCopyWithImpl<_SignInWithOtpDto>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$SignInWithOtpDtoToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _SignInWithOtpDto&&(identical(other.email, email) || other.email == email)&&(identical(other.challengeId, challengeId) || other.challengeId == challengeId)&&(identical(other.code, code) || other.code == code));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,email,challengeId,code);

@override
String toString() {
  return 'SignInWithOtpDto(email: $email, challengeId: $challengeId, code: $code)';
}


}

/// @nodoc
abstract mixin class _$SignInWithOtpDtoCopyWith<$Res> implements $SignInWithOtpDtoCopyWith<$Res> {
  factory _$SignInWithOtpDtoCopyWith(_SignInWithOtpDto value, $Res Function(_SignInWithOtpDto) _then) = __$SignInWithOtpDtoCopyWithImpl;
@override @useResult
$Res call({
 String email, String challengeId, String code
});




}
/// @nodoc
class __$SignInWithOtpDtoCopyWithImpl<$Res>
    implements _$SignInWithOtpDtoCopyWith<$Res> {
  __$SignInWithOtpDtoCopyWithImpl(this._self, this._then);

  final _SignInWithOtpDto _self;
  final $Res Function(_SignInWithOtpDto) _then;

/// Create a copy of SignInWithOtpDto
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? email = null,Object? challengeId = null,Object? code = null,}) {
  return _then(_SignInWithOtpDto(
email: null == email ? _self.email : email // ignore: cast_nullable_to_non_nullable
as String,challengeId: null == challengeId ? _self.challengeId : challengeId // ignore: cast_nullable_to_non_nullable
as String,code: null == code ? _self.code : code // ignore: cast_nullable_to_non_nullable
as String,
  ));
}


}

// dart format on

// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'api_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_ApiResponse _$ApiResponseFromJson(Map<String, dynamic> json) => _ApiResponse(
  success: json['success'] as bool? ?? false,
  detail: json['detail'] as String?,
);

Map<String, dynamic> _$ApiResponseToJson(_ApiResponse instance) =>
    <String, dynamic>{'success': instance.success, 'detail': instance.detail};

package com.example.myapplication.data.remote

import com.example.myapplication.data.model.AppNotification
import com.example.myapplication.data.model.CaregiverProfile
import com.example.myapplication.data.model.FamilyInvitation
import com.example.myapplication.data.model.FamilyMember
import com.example.myapplication.data.model.SosAlert
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface FamilyCareApiService {

    @GET("health/")
    suspend fun healthcheck(): HealthResponse

    @POST("auth/request-code/")
    suspend fun requestCode(@Body request: RequestCodeRequest): RequestCodeResponse

    @POST("auth/verify-code/")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): VerifyCodeResponse

    @POST("auth/register/")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login/")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/logout/")
    suspend fun logout(@Header("Authorization") authorization: String? = null): ApiOperationResponse

    @GET("dashboard/")
    suspend fun dashboard(@Header("Authorization") authorization: String? = null): DashboardDto

    @GET("me/profile/")
    suspend fun getProfile(@Header("Authorization") authorization: String? = null): CaregiverProfile

    @PATCH("me/profile/")
    suspend fun updateProfile(
        @Body profile: CaregiverProfile,
        @Header("Authorization") authorization: String? = null
    ): CaregiverProfile

    @GET("members/")
    suspend fun getMembers(@Header("Authorization") authorization: String? = null): MemberListResponse

    @GET("members/{memberId}/")
    suspend fun getMember(
        @Path("memberId") memberId: Int,
        @Header("Authorization") authorization: String? = null
    ): FamilyMember

    @PATCH("members/{memberId}/")
    suspend fun updateMember(
        @Path("memberId") memberId: Int,
        @Body member: FamilyMember,
        @Header("Authorization") authorization: String? = null
    ): FamilyMember

    @POST("members/{memberId}/location/")
    suspend fun updateMemberLocation(
        @Path("memberId") memberId: Int,
        @Body location: UpdateLocationRequest,
        @Header("Authorization") authorization: String? = null
    ): FamilyMember

    @GET("members/{memberId}/location-history/")
    suspend fun getMemberLocationHistory(
        @Path("memberId") memberId: Int,
        @Header("Authorization") authorization: String? = null
    ): LocationHistoryResponse

    @GET("invitations/")
    suspend fun getInvitations(@Header("Authorization") authorization: String? = null): InvitationListResponse

    @POST("invitations/")
    suspend fun sendInvitation(
        @Body payload: InvitationRequest,
        @Header("Authorization") authorization: String? = null
    ): FamilyInvitation

    @POST("invitations/{invitationId}/accept/")
    suspend fun acceptInvitation(
        @Path("invitationId") invitationId: Int,
        @Body payload: EmptyRequest = EmptyRequest(),
        @Header("Authorization") authorization: String? = null
    ): FamilyInvitation

    @DELETE("invitations/{invitationId}/")
    suspend fun deleteInvitation(
        @Path("invitationId") invitationId: Int,
        @Header("Authorization") authorization: String? = null
    ): ApiOperationResponse

    @GET("notifications/")
    suspend fun getNotifications(@Header("Authorization") authorization: String? = null): NotificationListResponse

    @POST("notifications/read-all/")
    suspend fun markAllNotificationsRead(
        @Body payload: EmptyRequest = EmptyRequest(),
        @Header("Authorization") authorization: String? = null
    ): NotificationListResponse

    @POST("notifications/{notificationId}/read/")
    suspend fun markNotificationRead(
        @Path("notificationId") notificationId: Int,
        @Body payload: EmptyRequest = EmptyRequest(),
        @Header("Authorization") authorization: String? = null
    ): AppNotification

    @DELETE("notifications/{notificationId}/")
    suspend fun deleteNotification(
        @Path("notificationId") notificationId: Int,
        @Header("Authorization") authorization: String? = null
    ): ApiOperationResponse

    @GET("activity-feed/")
    suspend fun getActivityFeed(@Header("Authorization") authorization: String? = null): ActivityFeedResponse

    @GET("sos/")
    suspend fun getSosAlerts(@Header("Authorization") authorization: String? = null): SosAlertListResponse

    @GET("sos/routes/")
    suspend fun getSosRoutes(@Header("Authorization") authorization: String? = null): SosRoutesResponse

    @POST("sos/trigger/")
    suspend fun triggerSos(
        @Body payload: EmptyRequest = EmptyRequest(),
        @Header("Authorization") authorization: String? = null
    ): SosAlert

    @POST("sos/{alertId}/resolve/")
    suspend fun resolveSos(
        @Path("alertId") alertId: Int,
        @Body payload: EmptyRequest = EmptyRequest(),
        @Header("Authorization") authorization: String? = null
    ): SosAlert
}

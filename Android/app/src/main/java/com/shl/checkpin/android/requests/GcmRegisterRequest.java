package com.shl.checkpin.android.requests;

import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import retrofit.http.Header;

/**
 * Created by vfedin on 28.11.2015.
 */
public interface GcmRegisterRequest {
    @FormUrlEncoded
    @POST(Requests.REGISTER_GCM)
    Response register(@Header("CheckPin-Agent") String appVersion, @Field("token")String uploadToken, @Field("gcmRegistrationToken")String googleToken);
}

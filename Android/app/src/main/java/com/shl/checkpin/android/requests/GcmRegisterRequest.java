package com.shl.checkpin.android.requests;

import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by vfedin on 28.11.2015.
 */
public interface GcmRegisterRequest {
    @FormUrlEncoded
    @POST(Requests.REGISTER_GCM)
    Response register(@Field("token")String uploadToken, @Field("gcmRegistrationToken")String googleToken);
}

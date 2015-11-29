package com.shl.checkpin.android.requests;

import com.shl.checkpin.android.dto.UploadTokenDTO;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by vfedin on 13.11.2015.
 */
public interface UploadTokenRequest {
    @GET(Requests.UPLOAD_AUTH_TOKEN)
    UploadTokenDTO get(@Query("id") String phoneNumber);
}

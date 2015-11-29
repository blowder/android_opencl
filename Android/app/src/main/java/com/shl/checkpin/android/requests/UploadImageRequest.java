package com.shl.checkpin.android.requests;

import retrofit.client.Response;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedInput;

/**
 * Created by vfedin on 13.11.2015.
 */


public interface UploadImageRequest {
    //TODO need server response object
    @Multipart
    @POST(Requests.IMAGE_UPLOAD)
    Response upload(@Part("token") String token,
                @Part("chunkNo") int chunkNo,
                @Part("chunksTotal") int chunksTotal,
                @Part("data") TypedInput data
    );

}

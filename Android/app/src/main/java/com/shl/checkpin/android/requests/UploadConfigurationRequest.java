package com.shl.checkpin.android.requests;

import com.shl.checkpin.android.dto.UploadConfDTO;
import retrofit.http.GET;

public interface UploadConfigurationRequest {
    @GET(Requests.UPLOAD_CONFIG)
    UploadConfDTO getConfiguration();
}
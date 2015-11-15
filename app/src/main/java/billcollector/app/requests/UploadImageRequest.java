package billcollector.app.requests;

import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;

/**
 * Created by vfedin on 13.11.2015.
 */


public interface UploadImageRequest {
    //TODO need server response object
    @Multipart
    @POST(Requests.IMAGE_UPLOAD)
    void upload(@Header("token") String token,
                @Header("chunkNo") int chunkNo,
                @Header("chunksTotal") int chunksTotal,
                @Part("data") byte[] data
    );

}

package billcollector.app.requests;

import billcollector.app.dto.UploadConf;
import retrofit.http.GET;

public interface UploadConfigurationRequest {
    @GET(Requests.UPLOAD_CONFIG)
    UploadConf getConfiguration();
}
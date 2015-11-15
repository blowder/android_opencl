package billcollector.app.requests;

import retrofit.http.POST;
import retrofit.http.Part;

/**
 * Created by vfedin on 13.11.2015.
 */
public interface AuthTokenRequest {
    @POST(Requests.AUTH_TOKEN)
    String get(@Part("userId") String userId);
}

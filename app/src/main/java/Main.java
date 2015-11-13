import billcollector.app.requests.UploadConfigurationRequest;
import billcollector.app.utils.Constants;
import retrofit.RestAdapter;

/**
 * Created by vfedin on 13.11.2015.
 */
public class Main {
    public static void main(String... args) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.SERVER_HOST)
                .build();

        UploadConfigurationRequest conf = restAdapter.create(UploadConfigurationRequest.class);
        System.out.println(conf.getConfiguration());
    }
}

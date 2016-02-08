import com.shl.checkpin.android.dto.UploadConfDTO;
import com.shl.checkpin.android.requests.*;
import com.shl.checkpin.android.utils.Constants;
import org.apache.commons.io.IOUtils;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.mime.TypedInput;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by vfedin on 13.11.2015.
 */
public class Main {
    public static void main(String... args) throws IOException {
      /*  File image = new File("C:\\ImageTestSuite\\png\\64bit.png");
        String googleToken = "asdfasdfasdfsdfsdfasdf";
        //hello
        RestAdapter uploadTokenAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.SERVER_HOST)
                .build();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.SERVER_HOST+":8080")
                .build();




        //auth
        UploadTokenRequest uploadTokenRequest = restAdapter.create(UploadTokenRequest.class);
        String token = uploadTokenRequest.get("380671023175").getToken();
        System.out.println(token);
        GcmRegisterRequest gcmRegisterRequest = restAdapter.create(GcmRegisterRequest.class);
        System.out.println(gcmRegisterRequest.register(token, googleToken).getStatus());

        //configuration
        UploadConfigurationRequest conf = uploadTokenAdapter.create(UploadConfigurationRequest.class);
        UploadConfDTO uploadConf = conf.getConfiguration();
        System.out.println(conf.getConfiguration());
        //send
        //byte[] fileInBytes = Files.toByteArray(image);
        byte[] fileInBytes = IOUtils.toByteArray(new FileInputStream(image));
        int chunkSize = uploadConf.getChunkSize();
        int chunksTotal = (int) Math.ceil(fileInBytes.length / (double)chunkSize);

        int chunkId = 1;
        int dataSize = 0;
        try {
            for (int i = 0; i < fileInBytes.length; i = i + chunkSize) {
                int leftLimit = i + chunkSize > fileInBytes.length ? fileInBytes.length : i + chunkSize;
                byte[] data = Arrays.copyOfRange(fileInBytes, i, leftLimit);
                dataSize += data.length;
                System.out.println("Chunk size " + data.length);

                //TODO timestamp send
                TypedInput typedBytes = new TypedByteArrayWithFilename("multipart/form-data",  data, image.getName());
                System.out.println(uploadTokenAdapter.create(UploadImageRequest.class).upload(token, chunkId, chunksTotal, typedBytes).getStatus());
                System.out.println("Chunk " + chunkId + " was sent!");
                chunkId++;
            }
            System.out.println("Transferred data " + dataSize);
        }catch (RetrofitError e ){
            System.out.println(e.getResponse().getStatus());
                System.out.println(new String(IOUtils.toByteArray(e.getResponse().getBody().in())));
        }*/

    }
}

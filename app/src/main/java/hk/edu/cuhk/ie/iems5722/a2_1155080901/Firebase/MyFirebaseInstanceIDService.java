package hk.edu.cuhk.ie.iems5722.a2_1155080901.Firebase;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by leoymr on 21/3/17.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    private String POST = "Post TOKEN";


    // This function will be invoked when Android assigns a token to the app
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        // Submit Token to your server (e.g. using HTTP) // (Implement your own logic ...)
        JSONObject json = null;

        RequestBody requestBody = new FormBody.Builder()
                .add("user_id", "1155080901")
                .add("token", token)
                .build();
        String url = "http://13.112.156.96/api/asgn4/submit_push_token";
        OkHttpClient client = new OkHttpClient();
        Log.d(POST, url);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String responseData = response.body().string();
            json = new JSONObject(responseData);
            Log.d(POST, String.valueOf(json.get("status")));
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(POST, "POST request error");
        }
    }
}

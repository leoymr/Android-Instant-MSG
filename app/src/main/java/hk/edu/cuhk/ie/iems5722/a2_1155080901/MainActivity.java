package hk.edu.cuhk.ie.iems5722.a2_1155080901;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import hk.edu.cuhk.ie.iems5722.a2_1155080901.Adapter.Main_ArrAdapter;
import hk.edu.cuhk.ie.iems5722.a2_1155080901.Data.Chatroom_INFO;
import hk.edu.cuhk.ie.iems5722.a2_1155080901.Firebase.MyFirebaseInstanceIDService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Main_ArrAdapter main_arrAdapter = null;
    private List<Chatroom_INFO> chatnameList = new ArrayList<>();

    private ListView mainListView;
    private String TAG = "mainActivity";
    private boolean flag = false;
    private String url = "http://13.112.156.96/api/asgn3/get_chatrooms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("IEMS 5722");

        mainListView = (ListView) findViewById(R.id.main_listview);
        mainListView.setDivider(null);

        new MainAsyncTask().execute(url);
        //Check Google Play Service available
        flag = isGooglePlayServicesAvailable(MainActivity.this);
        Log.d("isGPSAvailable", String.valueOf(flag));

        String token = FirebaseInstanceId.getInstance().getToken();
        if(token!=null){
            Log.d("Token: ",token);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Check Google Play Service available
        flag = isGooglePlayServicesAvailable(MainActivity.this);
        Log.d("isGPSAvailable", String.valueOf(flag));

    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    /**
     * AsyncTask class implementation
     */
    class MainAsyncTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... url) {
            JSONObject json = sendRequestWithOkHttp(url[0]);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            JSONArray jsonArray = null;
            try {
                jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    int id = jsonArray.getJSONObject(i).getInt("id");
                    String name = jsonArray.getJSONObject(i).getString("name");

                    Log.d(TAG, String.valueOf(name));

                    chatnameList.add(new Chatroom_INFO(id, name));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            main_arrAdapter = new Main_ArrAdapter(MainActivity.this, R.layout.chatroom_list, chatnameList);
            mainListView.setAdapter(main_arrAdapter);
            mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Chatroom_INFO chat_info = chatnameList.get(position);
                    Toast.makeText(MainActivity.this, String.valueOf(chat_info.getId()) + " " + chat_info.getChatroom_name(), Toast.LENGTH_SHORT)
                            .show();
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("chatroom_name", chat_info.getChatroom_name());
                    intent.putExtra("chatroom_id", String.valueOf(chat_info.getId()));

                    startActivity(intent);
                }
            });
        }

        /**
         * Sending GET request with Okhttp
         *
         * @param url chatroom message pages
         */
        private JSONObject sendRequestWithOkHttp(final String url) {
            Response response = null;
            JSONObject json = null;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try {
                response = client.newCall(request).execute();
                Log.d(TAG, String.valueOf(response.code()));

                String responseData = response.body().string();
                json = new JSONObject(responseData);
                if (json.get("status").equals("OK")) {
                    Log.d(TAG, "Chatroom connected!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return json;
        }
    }
}

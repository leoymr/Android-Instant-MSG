package hk.edu.cuhk.ie.iems5722.a2_1155080901;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import hk.edu.cuhk.ie.iems5722.a2_1155080901.Adapter.Chat_ArrAdapter;
import hk.edu.cuhk.ie.iems5722.a2_1155080901.Data.Send_INFO;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by leoymr on 29/1/17.
 */

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    //Log debug tag
    private String TAG = "chatActivity";
    private String PAR = "Parsing JSON";
    private String GET = "Get Request";
    private String POST = "Post Request";

    private Chat_ArrAdapter msg_arrAdapter = null;
    private List<Send_INFO> send_info = new ArrayList<>();

    private ListView listView;//Showing messages
    private Button btn_right;//Right sending button
    private EditText edit_msg;//Input msg

    private String title;
    private String chatroom_id;

    private Calendar calendar;
    private Calendar bef_calendar;

    private int cur_position = 0;//Current position
    private int total_pos = 0;//Current total items num
    private int total_item = 0;
    private int total_page;//Total page of each chatroom
    private int mPageNum = 1;//Current page

    private String name = "leoymr";//Set send user name

    private boolean SEND = false;//Click send button flag

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatting_room);
        getSupportActionBar().setDisplayShowHomeEnabled(true);//Show the back arrow

        Intent intent = getIntent();
        title = intent.getStringExtra("chatroom_name");
        chatroom_id = intent.getStringExtra("chatroom_id");
        //Get the room name & id from MainActivity
        setTitle(title);//Set ChatActivity title to current chatroom name

        listView = (ListView) findViewById(R.id.listview);

        listView.setDivider(null);//Set divider line invisible

        btn_right = (Button) findViewById(R.id.button_right);
        edit_msg = (EditText) findViewById(R.id.edittext_input);

        btn_right.setOnClickListener(this);
        new ChatAsyncTask().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * This part is using for judging what incident will happen
     * when clicking the refresh button
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_fresh:
                //Judging if current page is the first page to resend request
                if (mPageNum == 1) {
                    send_info.clear();//Clearing the list info
                    cur_position = 0;
                    total_pos = 0;
                    new ChatAsyncTask().execute();
                    listView.setSelection(listView.getCount() - 1);//Let listview show the bottom item
                } else {
                    mPageNum = 1;//Reset the page num to 1, in case after refreshing current page is not 1
                    send_info.clear();
                    cur_position = 0;
                    total_pos = 0;
                    new ChatAsyncTask().execute();
                    listView.setSelection(listView.getCount() - 1);//Let listview show the bottom item
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onClick(View v) {
        String msg = edit_msg.getText().toString().trim();
        SimpleDateFormat format;

        switch (v.getId()) {
            case R.id.button_right:
                if (msg.equals("")) {
                    Toast.makeText(getApplicationContext(), "Send can not be EMPTY !!!", Toast.LENGTH_SHORT).show();
                } else {
                    format = new SimpleDateFormat("HH:mm");
                    String time = format.format(new Date(System.currentTimeMillis()));
                    format = new SimpleDateFormat("yyyy-MM-dd");
                    String dateTime = format.format(new Date(System.currentTimeMillis()));
                    send_info.add(new Send_INFO(null, msg, name, time, dateTime));//Add right msg information
                    /*in case after sending a msg, load the same msg on the top of the list,
                      if page = total page, then should not remove the top one item*/
                    if (mPageNum != total_page) {
                        send_info.remove(0);
                    }else {
                        //reset the scroll position in case of jumping to the top of the listview
                        cur_position = 0;
                    }
                    listView.setSelection(listView.getCount()-1);
                    msg_arrAdapter.notifyDataSetChanged();
                    SEND = true;
                    new ChatAsyncTask().execute(msg);
                }
                break;
            default:
                break;
        }
        //Set edit text to null for the next input
        edit_msg.setText("");
    }

    /**
     * AsyncTask class implementation
     */
    class ChatAsyncTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... msg) {
            JSONObject json = null;
            if (SEND == true) {
                json = postRequestWithOkHttp(msg[0]);
                SEND = false;
                return null;
            } else {
                json = sendRequestWithOkHttp(mPageNum);
                return json;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            if (jsonObject != null) {

                parseJSONwithJSONObject(jsonObject);

                msg_arrAdapter = new Chat_ArrAdapter(ChatActivity.this, R.layout.item_list, send_info);
                msg_arrAdapter.notifyDataSetChanged();
                listView.setAdapter(msg_arrAdapter);

                Log.d("totalItem", String.valueOf(total_item));
                Log.d("cur_pos", String.valueOf(cur_position));
                Log.d("totalPOS", String.valueOf(total_pos));


                listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                    }

                    //Listening scroll to the top of the screen to load next page
                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        total_item = totalItemCount;
                        if (total_pos != totalItemCount) {
                            if (cur_position == 0) {
                            /*  If cur_position = 0, means is the first page, using cur_position to store
                                the difference of total item num and the current item num  */
                                cur_position = totalItemCount - total_pos;
                            } else {
                                /*!=1 means after send msg not jump to cur_position = 1
                                   (total_item - total_pos) means if there are less items in the last page, listview should scroll to there */
                                if ((total_item - total_pos) < cur_position && (total_item - total_pos) != 1) {
                                    cur_position = total_item - total_pos;
                                }
                                listView.setSelection(cur_position);
                                total_pos = totalItemCount;
                            }
                        }

                        if (firstVisibleItem == 0) {
                            View firstVisibleItemView = listView.getChildAt(0);
                            if (firstVisibleItemView != null && firstVisibleItemView.getTop() == 0) {
                                Log.d("ListView", "##### 滚动到顶部 #####");
                                //if scrolling to the top of a page, page ++
                                if (mPageNum < total_page) {
                                    mPageNum++;
                                    new ChatAsyncTask().execute();
                                    Toast.makeText(ChatActivity.this, "Loading",
                                            Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(ChatActivity.this, "Nothing More!!!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
            }
        }

        /**
         * This part is using for parse JSON data into String that need to display
         *
         * @param jsonObject
         */
        private void parseJSONwithJSONObject(JSONObject jsonObject) {
            //Dealing with the logic when adapter is set.
            try {
                SimpleDateFormat format, formatData;
                if (jsonObject.get("status").equals("OK")) {

                    Log.d(PAR, String.valueOf(jsonObject.get("status")));

                    JSONArray jsonArray = jsonObject.getJSONArray("data");

                    total_page = (int) jsonObject.get("total_pages");
                    Log.d("total page", String.valueOf(total_page));

                    int length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        //j using to store the next item in the list
                        int j = i + 1;
                        String time = jsonArray.getJSONObject(i).getString("timestamp");
                        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        //date stores the date of an item
                        Date date = format.parse(time);
                        calendar = Calendar.getInstance();
                        calendar.setTime(date);

                        String dateTime = null;
                        //in case list out of index
                        if (j < length) {
                            String time_after = jsonArray.getJSONObject(j).getString("timestamp");
                            formatData = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            //date_after store the next item datetime
                            Date date_after = formatData.parse(time_after);
                            bef_calendar = Calendar.getInstance();
                            bef_calendar.setTime(date_after);
                            //if the two items' date is different, show datetime of that day
                            if (getDaysBetween(bef_calendar, calendar) != 0) {
                                formatData = new SimpleDateFormat("yyyy-MM-dd");
                                dateTime = formatData.format(date);
                            }
                        } else if (mPageNum == total_page) {
                            //show the earliest item's date
                            formatData = new SimpleDateFormat("yyyy-MM-dd");
                            dateTime = formatData.format(date);
                        }
                        if (dateTime != null) {
                            Log.d("dateTime", dateTime);
                        }
                        //time stores the hour and minute
                        format = new SimpleDateFormat("HH:mm");
                        time = format.format(date);

                        String msg = jsonArray.getJSONObject(i).getString("message");
                        String name = jsonArray.getJSONObject(i).getString("name");

                        //In case there are null msg stored in the server.
                        if (msg.equals("")) {
                            continue;
                        } else {
                            if (name.equals("Anson") || name.equals("leoymr")) {
                                send_info.add(0, new Send_INFO(null, msg, name, time, dateTime));
                            } else {
                                send_info.add(0, new Send_INFO(msg, null, name, time, dateTime));
                            }
                        }
                    }

                } else {
                    Log.d(PAR, "Parsing error");
                    Log.d(PAR, String.valueOf(jsonObject.get("status")));
                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        /**
         * For get the days between two specific date
         *
         * @param d1
         * @param d2
         * @return Days between two calendar
         */

        public int getDaysBetween(Calendar d1, Calendar d2) {
            if (d1.after(d2)) {
                java.util.Calendar swap = d1;
                d1 = d2;
                d2 = swap;
            }
            int days = d2.get(Calendar.DAY_OF_YEAR) - d1.get(Calendar.DAY_OF_YEAR);
            int y2 = d2.get(Calendar.YEAR);
            if (d1.get(Calendar.YEAR) != y2) {
                d1 = (Calendar) d1.clone();
                do {
                    days += d1.getActualMaximum(Calendar.DAY_OF_YEAR);//得到当年的实际天数
                    d1.add(Calendar.YEAR, 1);
                } while (d1.get(Calendar.YEAR) != y2);
            }
            return days;
        }

        /**
         * Sending GET request with Okhttp
         *
         * @param page chatroom message pages
         */
        private JSONObject sendRequestWithOkHttp(int page) {
            JSONObject json = null;
            String url = "";
            if (chatroom_id != null) {
                url = String.format("http://13.112.156.96/api/asgn3/get_messages?%s&%s", "chatroom_id=" + chatroom_id, "page=" + page);
            }
            OkHttpClient client = new OkHttpClient();
            Log.d(GET, url);
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                json = new JSONObject(responseData);
                Log.d(GET, String.valueOf(json.get("status")));
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(GET, "GET Request error");
            }
            return json;
        }

        /**
         * Sending POST request of  with Okhttp
         *
         * @param msg messages
         */
        private JSONObject postRequestWithOkHttp(String msg) {
            JSONObject json = null;

            RequestBody requestBody = new FormBody.Builder()
                    .add("chatroom_id", chatroom_id)
                    .add("user_id", "1155080901")
                    .add("name", name)
                    .add("message", msg)
                    .build();
            String url = "http://13.112.156.96/api/asgn3/send_message";
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
            return json;
        }

    }
}




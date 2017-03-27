package hk.edu.cuhk.ie.iems5722.a2_1155080901.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import hk.edu.cuhk.ie.iems5722.a2_1155080901.Data.Send_INFO;
import hk.edu.cuhk.ie.iems5722.a2_1155080901.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by leoymr on 7/2/17.
 */

public class Chat_ArrAdapter extends ArrayAdapter<Send_INFO> {

    private int resourceId;

    public Chat_ArrAdapter(Context context, int resource, List<Send_INFO> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Send_INFO send_info = getItem(position);
        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.left = (LinearLayout) view.findViewById(R.id.left);
            viewHolder.left_name = (TextView) view.findViewById(R.id.left_name);
            viewHolder.text_left = (TextView) view.findViewById(R.id.text_left);
            viewHolder.text_time_left = (TextView) view.findViewById(R.id.text_clock_left);

            viewHolder.right = (LinearLayout) view.findViewById(R.id.right);
            viewHolder.right_name = (TextView) view.findViewById(R.id.right_name);
            viewHolder.text_right = (TextView) view.findViewById(R.id.text_right);
            viewHolder.text_time_right = (TextView) view.findViewById(R.id.text_clock_right);

            viewHolder.dateTime = (TextView) view.findViewById(R.id.dateTime);
            view.setTag(viewHolder);

        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag(); //Reacquire ViewHolder
        }

        String left = send_info.getLeft_text();
        String right = send_info.getRight_text();

        String dataTime = send_info.getDateTime();

        if (dataTime != null) {
            viewHolder.dateTime.setText(dataTime);
            viewHolder.dateTime.setVisibility(View.VISIBLE);
        } else {
            viewHolder.dateTime.setVisibility(View.GONE);
        }

        if (left == null) {
            //right msg coming, set left layout invisible
            viewHolder.text_right.setText(right);
            //show time format
            String time = send_info.getTextTime();
            viewHolder.text_time_right.setText(time);
            //set right name
            viewHolder.right_name.setText(send_info.getName());

            viewHolder.right.setVisibility(View.VISIBLE);
            viewHolder.left.setVisibility(View.GONE);
        }

        if (right == null) {
            //left msg coming, set right layout invisible
            viewHolder.text_left.setText(left);
            //show time format
            String time = send_info.getTextTime();
            viewHolder.text_time_left.setText(time);
            //set left name
            viewHolder.left_name.setText(send_info.getName());

            viewHolder.left.setVisibility(View.VISIBLE);
            viewHolder.right.setVisibility(View.GONE);
        }

        return view;
    }

    class ViewHolder {

        public TextView left_name;
        public TextView text_left;
        public TextView text_time_left;
        public LinearLayout left;

        public TextView right_name;
        public TextView text_right;
        public TextView text_time_right;
        public LinearLayout right;

        public TextView dateTime;

    }
}

package hk.edu.cuhk.ie.iems5722.a2_1155080901.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import hk.edu.cuhk.ie.iems5722.a2_1155080901.Data.Chatroom_INFO;
import hk.edu.cuhk.ie.iems5722.a2_1155080901.R;

/**
 * Created by leoymr on 13/2/17.
 */

public class Main_ArrAdapter extends ArrayAdapter<Chatroom_INFO> {

    private int resourceId;
//    private TextView chatroom_Name;

    public Main_ArrAdapter(Context context, int resource, List<Chatroom_INFO> objects) {
        super(context, resource, objects);
        this.resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Chatroom_INFO chatroom_info = getItem(position);
        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.chatroom_Name = (TextView) view.findViewById(R.id.chatroom_id);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        String chatroom = chatroom_info.getChatroom_name();
        viewHolder.chatroom_Name.setText(chatroom);

        return view;
    }

    class ViewHolder {
        public TextView chatroom_Name;
    }
}

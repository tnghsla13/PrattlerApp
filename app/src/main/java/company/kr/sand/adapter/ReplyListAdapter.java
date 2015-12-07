package company.kr.sand.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import company.kr.sand.R;
import company.kr.sand.controller.AppController;
import company.kr.sand.data.ReplyItem;

/**
 * Created by User on 2015-12-02.
 */
public class ReplyListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<ReplyItem> replyItems;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public ReplyListAdapter(Activity activity, List<ReplyItem> replyItems) {
        this.activity = activity;
        this.replyItems = replyItems;
    }

    @Override
    public int getCount() {
        return replyItems.size();
    }

    @Override
    public Object getItem(int location) {
        return replyItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        System.out.println("getView()");
        if (inflater == null)
            inflater =
                    (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.reply_item, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        TextView name = (TextView) convertView.findViewById(R.id.tv_name_reply);
        TextView body = (TextView) convertView.findViewById(R.id.tv_body_reply);
        NetworkImageView profilePic = (NetworkImageView) convertView.findViewById(R.id.profilePic_reply);

        ReplyItem item = replyItems.get(position);

        if (item != null) {
            name.setText(item.getName());
            body.setText(item.getBody());

            profilePic.setImageUrl(item.getProfilePic(), imageLoader);
            System.out.println(item.getName());

        } else {

            System.out.println("else clause");

        }


        return convertView;
    }


}

package company.kr.sand.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import company.kr.sand.views.FeedImageView;

import company.kr.sand.controller.AppController;
import company.kr.sand.data.FeedItem;
import company.kr.sand.R;

import java.util.List;

/**
 * Created by User on 2015-11-01.
 */
public class FeedListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<FeedItem> feedItems;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public FeedListAdapter(Activity activity, List<FeedItem> feedItems) {
        this.activity = activity;
        this.feedItems = feedItems;
    }

    @Override
    public int getCount() {
        return feedItems.size();
    }

    @Override
    public Object getItem(int location) {
        return feedItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.feed_item, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView timestamp = (TextView) convertView
                .findViewById(R.id.timestamp);
        TextView statusMsg = (TextView) convertView
                .findViewById(R.id.txtStatusMsg);
        TextView url = (TextView) convertView.findViewById(R.id.txtUrl);
        NetworkImageView profilePic = (NetworkImageView) convertView
                .findViewById(R.id.profilePic);
        FeedImageView feedImageView = (FeedImageView) convertView
                .findViewById(R.id.feedImage1);

        FeedItem item = feedItems.get(position);

        name.setText(item.getName());

        // Converting timestamp into x ago format
//        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
//                Long.parseLong(item.getTimeStamp()),
//                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        timestamp.setText(item.getTimeStamp());

        // Chcek for empty status message
        if (!TextUtils.isEmpty(item.getStatus())) {
            statusMsg.setText(item.getStatus());
            statusMsg.setVisibility(View.VISIBLE);
        } else {
            // status is empty, remove from view
            statusMsg.setVisibility(View.GONE);
        }

        // Checking for taste
        ImageView img_taste = (ImageView) convertView.findViewById(R.id.taste);
        String taste = item.getTaste();
        Log.d("asdf", "taste = " + taste);
        switch(taste) {
            case "0":
                img_taste.setImageResource(R.drawable.bad);
                break;
            case "1":
                img_taste.setImageResource(R.drawable.soso);
                break;
            case "2":
                img_taste.setImageResource(R.drawable.good);
                break;
        }

        // Checking for quantity
        ImageView img_quantity = (ImageView) convertView.findViewById(R.id.quantity);
        String quantity = item.getQuantity();
        //Log.d("asdf", "taste = " + taste);
        switch(taste) {
            case "0":
                img_quantity.setImageResource(R.drawable.bad);
                break;
            case "1":
                img_quantity.setImageResource(R.drawable.soso);
                break;
            case "2":
                img_quantity.setImageResource(R.drawable.good);
                break;
        }

        // Checking for performance
        ImageView img_performance = (ImageView) convertView.findViewById(R.id.performance);
        String performance = item.getPerformance();
        //Log.d("asdf", "taste = " + taste);
        switch(performance) {
            case "0":
                img_performance.setImageResource(R.drawable.bad);
                break;
            case "1":
                img_performance.setImageResource(R.drawable.soso);
                break;
            case "2":
                img_performance.setImageResource(R.drawable.good);
                break;
        }

        ImageView img_follow = (ImageView) convertView.findViewById(R.id.follow);

        // user profile pic
        profilePic.setImageUrl(item.getProfilePic(), imageLoader);

        // Feed image
        if (item.getImge() != null) {
            feedImageView.setImageUrl(item.getImge(), imageLoader);
            feedImageView.setVisibility(View.VISIBLE);
            feedImageView
                    .setResponseObserver(new FeedImageView.ResponseObserver() {
                        @Override
                        public void onError() {
                        }

                        @Override
                        public void onSuccess() {
                        }
                    });
        } else {
            feedImageView.setVisibility(View.GONE);
        }

        return convertView;
    }

}
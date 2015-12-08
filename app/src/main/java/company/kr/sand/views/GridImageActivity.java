package company.kr.sand.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import company.kr.sand.R;
import company.kr.sand.controller.AppController;
import company.kr.sand.data.FeedItem;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by User on 2015-11-12.
 */
public class GridImageActivity extends Activity {

    private Context mContext;
    private ImageLoader mImageLoader;
    private ArrayList<FeedItem> feed;
    private Button btn_reply;
    private FeedItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_item);
        mContext=getApplicationContext();

        mImageLoader = AppController.getInstance().getImageLoader();

        // get intent data
        Intent i = getIntent();

        // Selected image id
        int position = i.getExtras().getInt("position");
        feed = (ArrayList<FeedItem>) i.getSerializableExtra("imagelist");
        item = feed.get(position);

        TextView name = (TextView) findViewById(R.id.name);
        TextView timestamp = (TextView) findViewById(R.id.timestamp);
        TextView statusMsg = (TextView) findViewById(R.id.txtStatusMsg);
        TextView url = (TextView) findViewById(R.id.txtUrl);
        NetworkImageView profilePic = (NetworkImageView) findViewById(R.id.profilePic);
        FeedImageView feedImageView = (FeedImageView) findViewById(R.id.feedImage1);

        name.setText(item.getName());

        setReplyBtn();

        // Converting timestamp into x ago format
//        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
//                Long.parseLong(item.getTimeStamp()),
//                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        timestamp.setText(item.getTimeStamp());

        // Chcek for empty status message
        if (!TextUtils.isEmpty(item.getStatus())) {
            String body=item.getStatus();
            SpannableString contents=new SpannableString(body);
            ArrayList<int[]> hashSpan=getSpans(body,'#');

            for(int index=0; index<hashSpan.size();index++){
                int[] span = hashSpan.get(index);
                int hashStart=span[0];
                int hashEnd=span[1];

                contents.setSpan(new Hashtag(GridImageActivity.this),hashStart,hashEnd,0);
            }

            statusMsg.setText(contents);
            statusMsg.setVisibility(View.VISIBLE);
        } else {
            // status is empty, remove from view
            statusMsg.setVisibility(View.GONE);
        }

        // Checking for taste
        ImageView img_taste = (ImageView) findViewById(R.id.taste);
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
        ImageView img_quantity = (ImageView) findViewById(R.id.quantity);
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
        ImageView img_performance = (ImageView) findViewById(R.id.performance);
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

        ImageView img_follow = (ImageView) findViewById(R.id.follow);
        img_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do follow sql work
            }
        });


        // user profile pic
        profilePic.setImageUrl(item.getProfilePic(), mImageLoader);

        // Feed image
        if (item.getImge() != null) {
            feedImageView.setImageUrl(item.getImge(), mImageLoader);
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

    }

    private void setReplyBtn(){

        btn_reply=(Button)findViewById(R.id.btn_reply);
        btn_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Dialog dialog=new Dialog(GridImageActivity.this);
//                dialog.setContentView(R.layout.reply_dialog);
//                dialog.show();

                new ReplyDialog(GridImageActivity.this,item, mContext.getSharedPreferences("ID",MODE_PRIVATE).getString("remain",null)).show();

            }
        });
    }

    public ArrayList<int[]> getSpans(String body, char prefix) {
        ArrayList<int[]> spans = new ArrayList<int[]>();

        Pattern pattern = Pattern.compile(prefix + "\\w+");
        Matcher matcher = pattern.matcher(body);

        // Check all occurrences
        while (matcher.find()) {
            int[] currentSpan = new int[2];
            currentSpan[0] = matcher.start();
            currentSpan[1] = matcher.end();
            spans.add(currentSpan);
        }

        return  spans;
    }


}
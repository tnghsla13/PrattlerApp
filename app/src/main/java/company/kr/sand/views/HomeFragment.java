package company.kr.sand.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ViewFlipper;

import company.kr.sand.R;
import company.kr.sand.adapter.PicListAdapter;
import company.kr.sand.data.FeedItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private GridView gridView;
    private ViewFlipper viewFlipper;
    private float lastX;
    private PicListAdapter listAdapter;
    private ArrayList<FeedItem> feedItems;
    private String URL_FEED = "http://prattler.azurewebsites.net/feed1.php";
    public HomeFragment() {

    }

    private void parseJsonFeed(JSONObject response) {
        try {
            JSONArray feedArray = response.getJSONArray("feed");

            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                FeedItem item = new FeedItem();
                item.setId(feedObj.getInt("_idx"));
                item.setName(feedObj.getString("nickname"));

                String image = feedObj.isNull("b_imagepath") ? null : "http://prattler.azurewebsites.net" + (feedObj
                        .getString("b_imagepath")).substring(1);
                if(image == null)
                    continue;
                item.setImge(image);
                item.setStatus(feedObj.getString("body"));
                item.setProfilePic("http://prattler.azurewebsites.net" + (feedObj
                        .getString("imagepath")).substring(1));
                item.setTimeStamp(feedObj.getString("dtime"));
                item.setTaste(feedObj.getString("taste"));
                item.setQuantity(feedObj.getString("quantity"));
                item.setPerformance(feedObj.getString("performance"));

                feedItems.add(item);

            }
            // notify data changes to list adapater
            listAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){

            e.printStackTrace();
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_fragment, container, false);
        gridView = (GridView) view.findViewById(R.id.grid_view);
        feedItems = new ArrayList<FeedItem>();

        listAdapter = new PicListAdapter(getActivity(), feedItems);
        gridView.setAdapter(listAdapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), GridImageActivity.class);
                i.putExtra("position", position);
                i.putExtra("imagelist", feedItems);
                startActivity(i);
            }
        });

        viewFlipper = (ViewFlipper) view.findViewById(R.id.view_flipper);
        viewFlipper.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = motionEvent.getX();
                        return true;
                    case MotionEvent.ACTION_UP:
                        float currentX = motionEvent.getX();

                        if (lastX < currentX) {
                            if (viewFlipper.getDisplayedChild() == 0)
                                return true;

                            viewFlipper.setInAnimation(getActivity(), R.anim.slide_in_from_left);
                            viewFlipper.setOutAnimation(getActivity(), R.anim.slide_out_to_right);

                            viewFlipper.showNext();
                        }

                        if (lastX > currentX) {
                            if (viewFlipper.getDisplayedChild() == 1)
                                return true;

                            viewFlipper.setInAnimation(getActivity(), R.anim.slide_in_from_right);
                            viewFlipper.setOutAnimation(getActivity(), R.anim.slide_out_to_left);

                            viewFlipper.showPrevious();
                        }
                        return true;
                }
                return false;
            }
        });

        // We first check for cached request
        interactionWithWebServer();

        return view;
    }


    private void interactionWithWebServer() {

        AsyncTask<Void, Void, String> async_interact = new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(Void... params) {

                String url = null;
                URL urlCon = null;
                String response = null;

                try {

                    url = "http://prattler.azurewebsites.net/feed1.php";
                    urlCon = new URL(url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) urlCon.openConnection();

                    if (httpURLConnection != null) {

                        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setUseCaches(false);
                        httpURLConnection.setDefaultUseCaches(false);

                        OutputStream os = httpURLConnection.getOutputStream();
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "utf-8"));

                        SharedPreferences mPref=getActivity().getSharedPreferences("ID", Context.MODE_PRIVATE);

                        bw.write("id=" + mPref.getString("remain",null));
                        bw.flush();

                        InputStream is = httpURLConnection.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));

                        response = br.readLine();

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return response;
            }


            @Override
            protected void onPostExecute(String res) {
                System.out.println(res);
                try {
                    parseJsonFeed(new JSONObject(res));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        async_interact.execute();
    }


}
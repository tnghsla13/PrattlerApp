package company.kr.sand.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

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

import company.kr.sand.R;
import company.kr.sand.adapter.PicListAdapter;
import company.kr.sand.controller.AppController;
import company.kr.sand.data.FeedItem;

/**
 * A placeholder fragment containing a simple view.
 */
public class ProfileFragment extends Fragment {

    NetworkImageView img_profile;
    TextView tv_nick,tv_board, tv_follow, tv_follower;
    Button btn_modify;
    GridView grid_myfeed;
    View view;
    private ArrayList<FeedItem> feedItems;
    private PicListAdapter listAdapter;
    String url_profile;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    String nick;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        view=inflater.inflate(R.layout.profile_fragment, container, false);
        setView();
        interactionWithWebServer();

        return view;
    }


    private void parseJsonFeed(JSONArray response) {
        try {
            JSONArray feedArray = response;

            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                FeedItem item = new FeedItem();
                item.setId(feedObj.getInt("_idx"));
                item.setName(nick);

                // Image might be null sometimes
                String image = feedObj.isNull("b_imagepath") ? null : "http://prattler.azurewebsites.net" + (feedObj
                        .getString("b_imagepath")).substring(1);
                //Log.d("asdf", image);

                if(image == null)
                    continue;
                item.setImge(image);
                item.setStatus(feedObj.getString("body"));
                item.setProfilePic(url_profile);
                item.setTimeStamp(feedObj.getString("dtime"));
                item.setTaste(feedObj.getString("taste"));
                item.setQuantity(feedObj.getString("quantity"));
                item.setPerformance(feedObj.getString("performance"));

                // url might be null sometimes
                String feedUrl = feedObj.isNull("idx") ? null : feedObj
                        .getString("idx");


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



    private void setView(){

        feedItems=new ArrayList<FeedItem>();
        listAdapter=new PicListAdapter(getActivity(),feedItems);

        img_profile=(NetworkImageView)view.findViewById(R.id.img_profile);
        tv_nick=(TextView)view.findViewById(R.id.tv_nick);
        tv_board=(TextView)view.findViewById(R.id.tv_board);
        btn_modify=(Button)view.findViewById(R.id.btn_modify);
        tv_follow=(TextView)view.findViewById(R.id.tv_follow);
        tv_follower=(TextView)view.findViewById(R.id.tv_follower);
        grid_myfeed=(GridView)view.findViewById(R.id.grid_myfeed);
        grid_myfeed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), GridImageActivity.class);
                i.putExtra("position", position);
                i.putExtra("imagelist", feedItems);
                startActivity(i);
            }
        });
        grid_myfeed.setAdapter(listAdapter);
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

                    url = "http://prattler.azurewebsites.net/profile.php";
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
                profileSetting(res);
            }
        };
        async_interact.execute();
    }


    private void profileSetting(String res){

        try {
            JSONObject json_profile=new JSONObject(res);
            JSONArray json_info=json_profile.getJSONArray("profileinfo");
            JSONArray json_imgs=json_profile.getJSONArray("boardcontents");

            url_profile=json_info.getString(0);
            nick=json_info.getString(1);
            img_profile.setImageUrl(url_profile,imageLoader);

            // img_profile.setImageUrl(json_info.getString(0),imageLoader);
            //img_profile.setVisibility(View.VISIBLE);
            tv_nick.setText(nick);
            tv_board.setText(json_info.getString(2));
            tv_follow.setText(json_info.getString(3));
            tv_follower.setText(json_info.getString(4));

            parseJsonFeed(json_imgs);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}

package company.kr.sand.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import company.kr.sand.R;
import company.kr.sand.adapter.FeedListAdapter;
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
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class FriendsFragment extends Fragment {

    private static final String TAG = "FriendsFragment";
    private ListView listView;
    private FeedListAdapter listAdapter;
    private List<FeedItem> feedItems;
    private String URL_FEED = "http://prattler.azurewebsites.net/feed1.php";

    public FriendsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Parsing json reponse and passing the data to feed view list adapter
     * */
    private void parseJsonFeed(JSONObject response) {
        try {
            JSONArray feedArray = response.getJSONArray("feed");

            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                FeedItem item = new FeedItem();
                item.setId(feedObj.getInt("_idx"));
                item.setName(feedObj.getString("nickname"));

                // Image might be null sometimes
                String image = feedObj.isNull("b_imagepath") ? null : "http://prattler.azurewebsites.net" + (feedObj
                        .getString("b_imagepath")).substring(1);
                //Log.d("asdf", image);
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
        View view = inflater.inflate(R.layout.friends_fragment,container,false);
        listView = (ListView) view.findViewById(R.id.list);
        feedItems = new ArrayList<FeedItem>();

        listAdapter = new FeedListAdapter(getActivity(),feedItems);
        listView.setAdapter(listAdapter);

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

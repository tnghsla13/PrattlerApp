package company.kr.sand.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

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
import java.net.URLEncoder;
import java.util.ArrayList;

import company.kr.sand.R;
import company.kr.sand.adapter.ReplyListAdapter;
import company.kr.sand.data.FeedItem;
import company.kr.sand.data.ReplyItem;

/**
 * Created by Prattler on 2015-12-01.
 */
public class ReplyDialog extends Dialog  {


    private Context mContext;
    private Button btn_reply_in;
    private EditText et_reply;
    private FeedItem feedItem;
    private String id,body;

    private ReplyListAdapter listAdapter;
    private ListView listView;
    ArrayList<ReplyItem> replyItems;


    public ReplyDialog(Context context, FeedItem feed,String id) {
        super(context);
        this.mContext=context;
        this.feedItem=feed;
        this.id=id;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }


    @Override
    protected void onCreate(Bundle save) {
        super.onCreate(save);
        setContentView(R.layout.reply_dialog);
        setView();
        receiveFromWebServer();
    }

    private void setView(){
        btn_reply_in=(Button)findViewById(R.id.btn_reply_in);
        et_reply=(EditText)findViewById(R.id.et_reply);


        btn_reply_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                body = et_reply.getText().toString();
                et_reply.setText("");
                sendToWebServer();
                InputMethodManager imm= (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

                imm.hideSoftInputFromWindow(btn_reply_in.getWindowToken(), 0);

            }
        });

        listView = (ListView) findViewById(R.id.listView);

    }

    private void sendToWebServer(){

        AsyncTask<Void,Void,String> async_send=new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                try{
                    String url="http://prattler.azurewebsites.net/send.php";
                    URL urlCon=new URL(url);
                    HttpURLConnection httpURLConnection=(HttpURLConnection)urlCon.openConnection();


                    if (httpURLConnection != null) {

                        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setUseCaches(false);
                        httpURLConnection.setDefaultUseCaches(false);

                        OutputStream os = httpURLConnection.getOutputStream();
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "utf-8"));

                        bw.write("r_idx=" + feedItem.getId() + "&r_id=" + id + "&r_body=" + URLEncoder.encode(body, "utf-8"));
                        bw.flush();

                        InputStream is = httpURLConnection.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));

                        System.out.println(br.readLine());


                    }


                }catch(Exception e){
                    e.printStackTrace();
                }



                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                receiveFromWebServer();
            }
        };

        async_send.execute();
    }

    private void receiveFromWebServer(){

        AsyncTask<String,Void,String> async_receive=new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... Params) {

                String url = null;
                URL urlCon = null;
                String response = null;

                try {

                    url = "http://prattler.azurewebsites.net/reply.php";
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

                        bw.write("r_idx=" + feedItem.getId());
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
                    parseJsonFeed(new JSONArray(res));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        async_receive.execute();
    }

    private void parseJsonFeed(JSONArray response) {
        try {
            JSONArray feedArray = response;
            replyItems = new ArrayList<ReplyItem>();
            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                ReplyItem item = new ReplyItem();
                item.setName(feedObj.getString("nickname"));
                item.setBody(feedObj.getString("r_body"));
                item.setProfilePic("http://prattler.azurewebsites.net" + (feedObj
                        .getString("imagepath")).substring(1));



                replyItems.add(item);


            }

            listAdapter = new ReplyListAdapter((Activity)mContext,replyItems);
            listView.setAdapter(listAdapter);

            // notify data changes to list adapater
            listAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){

            e.printStackTrace();
        }
    }


}
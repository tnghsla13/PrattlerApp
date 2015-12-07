package company.kr.sand.registration;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.nhn.android.naverlogin.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import company.kr.sand.R;
import company.kr.sand.views.MainActivity;

/**
 * Created by Prattler on 2015-10-27.
 */

public class LoginActivity extends FragmentActivity {

    //request code
    public static final int CONNECTION_WITH_LOGIN = 0;
    public static final int CONNECTION_FOR_REDUNDANCY_CHECK = 2;
    public static final int REG_COMPLETE = 3;

    //result string
    public static final String ACCOUNT_NOT_EXIST = "account_not_exist ";
    public static final String ACCOUNT_EXIST = "account_exist ";
    public static final String NICK_NOT_EXIST = "nick_not_exist ";
    public static final String NICK_EXIST = "nick_exist ";
    public static final String PERFECT = "perfect";

    //redundancy check
    public static boolean REDUNDANCY_CHECK = false;

    private ImageView img_egg;
    private String str_nick;
    private Context mContext; //application context

    private CallbackManager callbackManager; //facebook event callback
    private LoginManager loginManager; //facebook login management
    private OAuthLogin mOAuthLoginInstance; //naver login subject
    private OAuthLoginHandler mOAuthLoginHandler; //naver login management
    private UserInfo userInfo;
    private HttpURLConnection httpURLConnection;
    private ImageRegFragment fg_img;
    private LoginFragment fg_login;
    private NickRegFragment fg_nick;
    private InputStream is;
    private OutputStream os;
    private BufferedWriter bw;
    private BufferedReader br;

    private FragmentManager.OnBackStackChangedListener backStackListener;

    //backstack에 추가 되는 시점을 잡는다.
    private void setBackStackListener() {

        backStackListener = new FragmentManager.OnBackStackChangedListener() {

            @Override
            public void onBackStackChanged() {
                String name = null;
                int position = getSupportFragmentManager().getBackStackEntryCount();

                if (position != 0) {
                    FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().getBackStackEntryAt(position - 1);
                    name = backEntry.getName();
                }

                if (position == 0) {
                    finish();

                } else if (name == "login_frag") {


                    FacebookSdk.sdkInitialize(mContext);
                    callbackManager = CallbackManager.Factory.create();
                    userInfo = new UserInfo();
                    facebookLoginInit();

                    // naver
                    mOAuthLoginInstance = OAuthLogin.getInstance();
                    mOAuthLoginInstance.init(mContext, getResources().getString(company.kr.sand.R.string.naver_app_id),
                            getResources().getString(company.kr.sand.R.string.naver_app_secret), getResources().getString(R.string.app_name));


                    naverLoginPrecessInit();

                    REDUNDANCY_CHECK = false;

                    img_egg=(ImageView)findViewById(R.id.img_egg);
                    img_egg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            System.out.println("onclick");
                            fuckingMento("1");
                        }
                    });



                } else if (name == "nick_frag") {

                    Button btn_next_alias = (Button) findViewById(R.id.btn_next_alias);
                    Button btn_redundancy = (Button) findViewById(R.id.btn_redunancy);
                    final EditText tf_nick = (EditText) findViewById(R.id.tf_nick);
                    tf_nick.setText("");

                    btn_next_alias.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (REDUNDANCY_CHECK) {

                                if (str_nick.equals(tf_nick.getText().toString()))
                                    setFragment(2);
                                else
                                    Toast.makeText(mContext, "중복확인이 필요합니다", Toast.LENGTH_SHORT).show();
                            } else {

                                Toast.makeText(mContext, "중복확인이 필요합니다", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    btn_redundancy.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            str_nick = tf_nick.getText().toString();

                            if (str_nick.length() < 2 || str_nick.length() > 8) {

                                Toast.makeText(mContext, "2글자 이상 8글자 이하 입력", Toast.LENGTH_SHORT).show();

                            } else {

                                interactionWithWebServer(CONNECTION_FOR_REDUNDANCY_CHECK);
                            }

                        }
                    });


                } else {

                    Button btn_next_img = (Button) findViewById(R.id.btn_next_image);

                    btn_next_img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            interactionWithWebServer(REG_COMPLETE);
                        }
                    });

                }

            }
        };

        getSupportFragmentManager().addOnBackStackChangedListener(backStackListener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        callbackManager.onActivityResult(requestCode, resultCode, data);
        fg_img.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mContext = getApplicationContext();

        fg_img = new ImageRegFragment();
        fg_login = new LoginFragment();
        fg_nick = new NickRegFragment();

        setBackStackListener();
        setFragment(0);


    }

    private void setFragment(int kind) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();


        switch (kind) {

            case 0:

                ft.replace(R.id.fragment_container, fg_login);
                ft.addToBackStack("login_frag");
                ft.commit();

                break;
            case 1:

                ft.replace(R.id.fragment_container, fg_nick);
                ft.addToBackStack("nick_frag");
                ft.commit();

                break;
            case 2:

                ft.replace(R.id.fragment_container, fg_img);
                ft.addToBackStack("img_frag");
                ft.commit();

                break;
        }

    }

    //Login manager Setting
    private void setLoginManager() {

        loginManager = LoginManager.getInstance();
        loginManager.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        //To get user information - Graph Api
                        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),

                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        // Application code

                                        try {
                                            userInfo.id = object.getString("id");
                                            userInfo.age_range = object.getString("age_range");
                                            userInfo.email = object.getString("email");
                                            userInfo.gender = object.getString("gender");
                                            userInfo.name = object.getString("name");


                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        interactionWithWebServer(CONNECTION_WITH_LOGIN);


                                    }
                                });

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id, name, first_name, last_name, age_range, " +
                                "link, gender, locale, timezone, updated_time," +
                                "verified, email");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });

    }

    //facebook initialize
    private void facebookLoginInit() {

        setLoginManager();
        final Button btn_facebook = (Button) findViewById(R.id.btn_facebook);
        btn_facebook.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

//                loginManager.logOut();
                ArrayList<String> permissionList = new ArrayList<String>();
                permissionList.add("public_profile");
                permissionList.add("email");
                loginManager.logInWithReadPermissions(LoginActivity.this, permissionList);

            }
        });
    }

    //AuthLoginhandler setting
    private void setMOAuthLoginHandler() {
        mOAuthLoginHandler = new OAuthLoginHandler() {

            @Override
            public void run(boolean success) {

                if (success) {

                    new RequestApiTask().execute(); //로그인이 성공하면  네이버에 계정값들을 가져온다.


                } else {// 실패 시


                }
            }
        };
    }

    //naver initialize
    private void naverLoginPrecessInit() {

        setMOAuthLoginHandler();
        final Button btn_naver = (Button) findViewById(R.id.btn_naver);
        btn_naver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mOAuthLoginInstance.startOauthLoginActivity(LoginActivity.this, mOAuthLoginHandler);

            }
        });
    }


    //To get user information - Async task
    private class RequestApiTask extends AsyncTask<Void, Void, Void> {

        String resultSet = null;

        @Override
        protected Void doInBackground(Void... params) {

            String url = "https://openapi.naver.com/v1/nid/getUserProfile.xml";
            String at = mOAuthLoginInstance.getAccessToken(mContext);
            resultSet = mOAuthLoginInstance.requestApi(mContext, at, url);
            Pasingversiondata(resultSet);

            return null;
        }


        protected void onPostExecute(Void content) {

            interactionWithWebServer(CONNECTION_WITH_LOGIN);

        }


        //XML parsing
        private void Pasingversiondata(String data) { // xml 파싱

            String arr_info[] = new String[9];

            try {

                XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
                XmlPullParser parser = parserCreator.newPullParser();
                InputStream input = new ByteArrayInputStream(data.getBytes("UTF-8"));
                parser.setInput(input, "UTF-8");

                int parserEvent = parser.getEventType();
                String tag;
                boolean isText = false;
                int Idx = 0;


                while (parserEvent != XmlPullParser.END_DOCUMENT) {

                    switch (parserEvent) {

                        case XmlPullParser.START_TAG:

                            tag = parser.getName();

                            if (tag.compareTo("xml") == 0) {

                                isText = false;

                            } else if (tag.compareTo("data") == 0) {

                                isText = false;

                            } else if (tag.compareTo("result") == 0) {

                                isText = false;

                            } else if (tag.compareTo("resultcode") == 0) {

                                isText = false;

                            } else if (tag.compareTo("message") == 0) {

                                isText = false;

                            } else if (tag.compareTo("response") == 0) {

                                isText = false;

                            } else {

                                isText = true;

                            }

                            break;

                        case XmlPullParser.TEXT:

                            tag = parser.getName();

                            if (isText) {

                                if (parser.getText() == null) {

                                    arr_info[Idx] = "";

                                } else {

                                    arr_info[Idx] = parser.getText().trim();

                                }

                                Idx++;

                            }

                            isText = false;

                            break;

                        case XmlPullParser.END_TAG:

                            tag = parser.getName();

                            isText = false;

                            break;

                    }

                    parserEvent = parser.next();

                }

                userInfo.email = arr_info[0];
                userInfo.age_range = arr_info[4];
                userInfo.gender = arr_info[5];
                userInfo.id = arr_info[6];
                userInfo.name = arr_info[7];


            } catch (Exception e) {

                Log.e("dd", "Error in network call", e);

            }
        }
    }

    // our server connection part[

    private void interactionWithWebServer(final int flag) {

        final ProgressDialog progressDialog = new ProgressDialog(mContext);
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

                    switch (flag) {

                        case CONNECTION_WITH_LOGIN:

                            //학교 ip http://165.194.34.39:98/sandserverside.php
                            //집 ip http://192.168.35.197:80/sandserverside.php


                            url = "http://prattler.azurewebsites.net/sandserverside.php";
                            urlCon = new URL(url);
                            httpURLConnection = (HttpURLConnection) urlCon.openConnection();

                            if (httpURLConnection != null) {

                                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                httpURLConnection.setRequestMethod("POST");
                                httpURLConnection.setDoOutput(true);
                                httpURLConnection.setDoInput(true);
                                httpURLConnection.setUseCaches(false);
                                httpURLConnection.setDefaultUseCaches(false);

                                os = httpURLConnection.getOutputStream();
                                bw = new BufferedWriter(new OutputStreamWriter(os, "utf-8"));

                                bw.write("id=" + userInfo.id + "&type=login");
                                bw.flush();

                                is = httpURLConnection.getInputStream();
                                br = new BufferedReader(new InputStreamReader(is, "utf-8"));

                                response = br.readLine();


                            }
                            break;

                        case CONNECTION_FOR_REDUNDANCY_CHECK:

                            url = "http://prattler.azurewebsites.net/sandserverside.php";
                            urlCon = new URL(url);
                            httpURLConnection = (HttpURLConnection) urlCon.openConnection();

                            if (httpURLConnection != null) {

                                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                httpURLConnection.setRequestMethod("POST");
                                httpURLConnection.setDoOutput(true);
                                httpURLConnection.setDoInput(true);
                                httpURLConnection.setUseCaches(false);
                                httpURLConnection.setDefaultUseCaches(false);

                                os = httpURLConnection.getOutputStream();
                                bw = new BufferedWriter(new OutputStreamWriter(os, "utf-8"));

                                bw.write("nick=" + str_nick + "&type=nick");
                                bw.flush();

                                is = httpURLConnection.getInputStream();
                                br = new BufferedReader(new InputStreamReader(is, "utf-8"));

                                response = br.readLine();
                            }


                            break;

                        case REG_COMPLETE:

                            //multipart방식의 문자열 전송에 문제가 있어 get방식으로 변경
                            url = "http://prattler.azurewebsites.net/sandserverside2.php?" +
                                    "id="+userInfo.id+"&name="+URLEncoder.encode(userInfo.name,"utf-8")+"&gender="+userInfo.gender+"&email="+userInfo.email+
                                    "&age_range="+ userInfo.age_range+"&nick="+URLEncoder.encode(str_nick,"utf-8");
                            urlCon = new URL(url);
                            httpURLConnection = (HttpURLConnection) urlCon.openConnection();

                            String boundary = "************";
                            String twoHyphens = "--";
                            String lineEnd = "\r\n";

                            if (httpURLConnection != null) {

                                httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                                httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                                httpURLConnection.setRequestMethod("POST");
                                httpURLConnection.setDoOutput(true);
                                httpURLConnection.setDoInput(true);
                                httpURLConnection.setUseCaches(false);
                                httpURLConnection.setDefaultUseCaches(false);

                                String fileName = fg_img.getImgPath();

                                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());

                                //이미지 작성 부분
                                if (fileName != null) {

                                    FileInputStream fis = new FileInputStream(fileName);
                                    int bytesAvailable = fis.available();
                                    int maxBufferSize = 1024;
                                    int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                                    byte[] buffer = new byte[bufferSize];


                                    wr.writeBytes(twoHyphens + boundary + lineEnd);
                                    wr.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + fileName + "\"" + lineEnd);
                                    wr.writeBytes(lineEnd);

                                    int bytesRead = fis.read(buffer, 0, bufferSize);
                                    while (bytesRead > 0) {
                                        wr.write(buffer, 0, bufferSize);
                                        bytesAvailable = fis.available();
                                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                                        bytesRead = fis.read(buffer, 0, bufferSize);
                                    }

                                    wr.writeBytes(lineEnd);

                                    fis.close();
                                }

                                //전송의 끝을 알림
                                wr.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                                wr.flush();
                                // 전송 완료 부분

                                is = httpURLConnection.getInputStream();
                                br = new BufferedReader(new InputStreamReader(is, "utf-8"));

                                response = br.readLine();


                            }


                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


                return response;
            }

            @Override
            protected void onPostExecute(String res) {

                System.out.println(res);

                if (res.equals(ACCOUNT_NOT_EXIST)) setFragment(1);

                if(res.equals(ACCOUNT_EXIST)){

                    Toast.makeText(mContext, "가입된  계정", Toast.LENGTH_SHORT).show();
                    Intent it_next = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(it_next);
                    finish();

                }

                if (res.equals(NICK_NOT_EXIST)) {
                    Toast.makeText(mContext, "사용가능한 닉네임", Toast.LENGTH_SHORT).show();
                    REDUNDANCY_CHECK = true;
                }

                if (res.equals(PERFECT)) {

                    Toast.makeText(mContext, "가입완료", Toast.LENGTH_SHORT).show();

                    SharedPreferences mPref = getSharedPreferences("ID", MODE_PRIVATE);
                    SharedPreferences.Editor se=mPref.edit();
                    se.putString("remain",userInfo.id);
                    se.commit();

                    //명환 코드로 이동
                    Intent it_next = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(it_next);
                    finish();
                }

            }
        };

        async_interact.execute();
    }


    @Override
    protected void onResume() {
        super.onResume();


    }

    private void fuckingMento(final String admin) {

        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        AsyncTask<Void, Void, String> async_interact = new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(Void... params) {

                String url = null;
                URL urlCon = null;
                String response = null;

                try{

                    url = "http://prattler.azurewebsites.net/sandserverside.php";
                    urlCon = new URL(url);
                    httpURLConnection = (HttpURLConnection) urlCon.openConnection();

                    if (httpURLConnection != null) {

                        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setUseCaches(false);
                        httpURLConnection.setDefaultUseCaches(false);

                        os = httpURLConnection.getOutputStream();
                        bw = new BufferedWriter(new OutputStreamWriter(os, "utf-8"));

                        bw.write("id=1&type=login");
                        bw.flush();

                        is = httpURLConnection.getInputStream();
                        br = new BufferedReader(new InputStreamReader(is, "utf-8"));

                        response = br.readLine();
                    }

                }catch (Exception e){

                    e.printStackTrace();
                }


                return response;
            }

            @Override
            protected void onPostExecute(String res) {
                System.out.println(res);
                if(res.equals(ACCOUNT_EXIST)){

                    Toast.makeText(mContext, "가입된  계정", Toast.LENGTH_SHORT).show();
                    Intent it_next = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(it_next);
                    finish();

                }
            }
        };

        async_interact.execute();
    }


}
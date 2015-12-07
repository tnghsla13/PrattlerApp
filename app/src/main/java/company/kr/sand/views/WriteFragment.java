package company.kr.sand.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import company.kr.sand.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class WriteFragment extends Fragment {

    public static final int HIGH = 2;
    public static final int MIDDLE = 1;
    public static final int LOW = 0;

    public static final int REQUEST_CODE_IMAGE = 0;
    View view;
    Button btn_image_reg;
    Button btn_post;
    EditText editText;
    //Button btn_next_image;
    RelativeLayout food_image_back;
    Activity activity = getActivity();
    RadioGroup rg_taste, rg_quantity, rg_performance;
    int cur_taste, cur_quantity, cur_performance;
    String urlstr = "http://prattler.azurewebsites.net/write.php";
    BackgroundTask task;
    String imagePath;



    public WriteFragment() {
    }

    class BackgroundTask extends AsyncTask<Integer, Integer, Integer> {


        String crlf = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";
        String body;

        ProgressDialog pd;

        int maxBufferSize;
        int byteRead;
        byte[] buffer;


        DataOutputStream dos;
        FileInputStream fis;

        protected void onPreExecute() {

            pd = ProgressDialog.show(activity,"업로딩 중", "잠시만 기다려주세요",true,false);

            body = editText.getText().toString();
            maxBufferSize = 1*1024*1024;
        }

        @Override
        protected Integer doInBackground(Integer... arg0) {
            try{

                SharedPreferences mPref=getActivity().getSharedPreferences("ID", Context.MODE_PRIVATE);
                String userid=mPref.getString("remain",null);
                System.out.println(mPref.getString("remain",null));
                // http conntection
                URL url = new URL(urlstr+"?userid="+userid+"&taste="+String.valueOf(cur_taste)+"&quantity="+String.valueOf(cur_quantity)
                        +"&performance="+String.valueOf(cur_performance)+"&body="+URLEncoder.encode(body,"utf-8"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();


                // set options
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);

                // set property
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                // outputstream init

                dos = new DataOutputStream(conn.getOutputStream());

                if(imagePath!=null){

                    System.out.println(imagePath);
                    fis = new FileInputStream(imagePath);
                    writeImage(imagePath);

                    dos.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
                    dos.flush();
                    dos.close();
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
                StringBuilder inputLine = new StringBuilder();
                String input;

                while((input = in.readLine()) != null) {
                    System.out.println(input);
                }

            } catch (MalformedURLException ex) {
                Log.d("asdf","malform");
            } catch(ProtocolException ex) {
                Log.d("asdf","Protocol exception");
            } catch (IOException ex) {
               Log.d("asdf", "IOException" + ex.toString() );
            }
            return null;
        }

        @SuppressLint("NewApi")
        protected void onPostExecute(Integer a) {

            imagePath = null;
            rg_taste.check(R.id.btn_taste_high);
            cur_taste=HIGH;
            rg_quantity.check(R.id.btn_quantity_high);
            cur_quantity=HIGH;
            rg_performance.check(R.id.btn_performance_high);
            cur_performance=HIGH;
            editText.setText("");
            pd.dismiss();

            food_image_back.setBackground(getResources().getDrawable(R.drawable.profile));
        }

        protected void writeImage(String fileName) {
            try {
                // write file sending headers
                dos.writeBytes(twoHyphens+boundary+crlf);
                dos.writeBytes("Content-Disposition: form-data; name=file; filename=\""+fileName+ "\""+crlf);
                dos.writeBytes(crlf);

                // sending file by byte array
                int bytesAvailable = fis.available();
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                byteRead = fis.read(buffer,0,bufferSize);

                while(byteRead > 0) {
                    dos.write(buffer,0,bufferSize);
                    bytesAvailable = fis.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    byteRead = fis.read(buffer,0,bufferSize);
                }

                dos.writeBytes(crlf);
            } catch (IOException ex) {
                Log.d("asdf","image fail");
            }
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cur_taste = HIGH;
        cur_quantity = HIGH;
        cur_performance = HIGH;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.write_fragment, container, false);
        setView();
        setViewAction();

        return view;
    }

    private void setView() {

        btn_image_reg = (Button) view.findViewById(R.id.btn_image_reg_food);
        food_image_back = (RelativeLayout) view.findViewById(R.id.img_food_back);
        rg_taste = (RadioGroup) view.findViewById(R.id.rg_taste);
        rg_quantity = (RadioGroup) view.findViewById(R.id.rg_quantity);
        rg_performance = (RadioGroup) view.findViewById(R.id.rg_performance);
        btn_post = (Button) view.findViewById(R.id.btn_post);
        editText = (EditText) view.findViewById(R.id.editText);
    }

    private void setViewAction() {

        rg_taste.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch (i) {
                    case R.id.btn_taste_low:
                        cur_taste = LOW;
                        break;
                    case R.id.btn_taste_middle:
                        cur_taste = MIDDLE;
                        break;
                    case R.id.btn_taste_high:
                        cur_taste = HIGH;
                        break;

                }
            }
        });

        rg_quantity.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch (i) {
                    case R.id.btn_quantity_low:
                        cur_quantity = LOW;
                        break;
                    case R.id.btn_quantity_middle:
                        cur_quantity = MIDDLE;
                        break;
                    case R.id.btn_quantity_high:
                        cur_quantity = HIGH;
                        break;

                }
            }
        });

        rg_performance.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch (i) {
                    case R.id.btn_peformance_low:
                        cur_performance = LOW;
                        break;
                    case R.id.btn_performance_middle:
                        cur_performance = MIDDLE;
                        break;
                    case R.id.btn_performance_high:
                        cur_performance = HIGH;
                        break;

                }
            }
        });


        btn_image_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_IMAGE);
            }
        });

        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imagePath != null) {
                    new BackgroundTask().execute();
                } else {
                    Toast.makeText(activity, "사진을 선택해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        activity = getActivity();


    }

    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE && resultCode == activity.RESULT_OK && data != null) {
            final Uri selectImageUri = data.getData();
            final String[] filePathColumn = {MediaStore.Images.Media.DATA};
            final Cursor imageCursor = activity.getContentResolver().query(selectImageUri, filePathColumn,
                    null, null, null);

            imageCursor.moveToFirst();

            final int columnIndex = imageCursor.getColumnIndex(filePathColumn[0]);
            imagePath = imageCursor.getString(columnIndex);
            imageCursor.close();
            final Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            Drawable back_drawable = new BitmapDrawable(getResources(), bitmap);
            food_image_back.setBackground(back_drawable);
        }
    }
}

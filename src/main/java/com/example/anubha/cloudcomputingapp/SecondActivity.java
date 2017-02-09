package com.example.anubha.cloudcomputingapp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import android.util.Base64;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class SecondActivity extends Activity {
    private ImageView ivImage;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(this);
        setContentView(R.layout.activity_2);
        Bitmap thumbnail = this.getIntent().getParcelableExtra("bmp");

        GetRequest gr = new GetRequest();
        gr.execute(thumbnail);

        ivImage = (ImageView) findViewById(R.id.ivImage);
        ivImage.setImageBitmap(thumbnail);
        //TextView textView2 = (TextView) findViewById(R.id.textView2);
        //textView2.setText("Fancy"); // this will be passed from the neural network
        //TextView textView4 = (TextView) findViewById(R.id.textView4);
        //textView4.setText("Good for Kids");
    }

    private class GetRequest extends AsyncTask<Bitmap, Void, String> {

        private HttpURLConnection connection = null;
        private String response = null;
        private Exception exception;

        @Override
        protected String doInBackground(Bitmap[] objects) {
            // encode image
            Bitmap image = objects[0];
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, os);
            String imstring = Base64.encodeToString(os.toByteArray(), Base64.DEFAULT);
            try {
                // build json
                JSONObject json = new JSONObject();
                JSONArray size = new JSONArray();
                // don't need size
                size.put(1);
                size.put(1);
                json.put("size", size);
                json.put("image", imstring);
                // get connection
                URL url = new URL("http://nn-flask-env.vm8fiwyr7p.us-east-1.elasticbeanstalk.com/");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.connect();;
                //connection.setRequestProperty("Content-Length", String.valueOf(json.toString().length()));
                // send request
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(json.toString());
                wr.flush();
                wr.close();
                // get response
                //int status = connection.getResponseCode();
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                this.response = response.toString();
            } catch (Exception e) {
                Log.d("ERROR", getStackTrace(e));
                this.exception = e;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response){
            //parse into JSON
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);
                TextView textView4 = (TextView) findViewById(R.id.textView4);
                textView4.setText(String.valueOf(jsonObject.getDouble("Casual Elegant")));
                TextView textView6 = (TextView) findViewById(R.id.textView6);
                textView6.setText(String.valueOf(jsonObject.getDouble("Fine Dining")));
                TextView textView8 = (TextView) findViewById(R.id.textView8);
                textView8.setText(String.valueOf(jsonObject.getDouble("Casual Dining")));
            } catch (JSONException e) {
                Log.d("ERROR",getStackTrace(e));
            }
        }

        private String getStackTrace(final Throwable throwable) {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw, true);
            throwable.printStackTrace(pw);
            return sw.getBuffer().toString();
        }

    }

}

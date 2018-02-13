package com.uncc.mobileappdev.inclass05;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> keywords = new ArrayList<>();
    private ArrayList<String> imageLinks;
    private String selectedKeyword;
    private int imageIndex = 0;
    EditText search;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search = (EditText) findViewById(R.id.editText_search);
        imageView = (ImageView) findViewById(R.id.photoDisplay);

        Log.d("Demo","Created");

        if(isConnected()){
            Log.d("Demo","Fetching data");
            new GetDataUsingGetAsync().execute("http://dev.theappsdr.com/apis/photos/keywords.php");
        }

        findViewById(R.id.button_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(keywords);
            }
        });

        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageLinks.isEmpty()){
                    Toast toast = Toast.makeText(MainActivity.this,"No images returned!", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    if (imageIndex != imageLinks.size() - 1) {
                        imageIndex++;
                        new GetImageAsync(imageView).execute();
                    } else {
                        imageIndex = 0;
                        new GetImageAsync(imageView).execute();
                    }
                }
            }
        });

        findViewById(R.id.prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageLinks.isEmpty()){
                    Toast toast = Toast.makeText(MainActivity.this,"No images returned!", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    if (imageIndex != 0) {
                        imageIndex--;
                        new GetImageAsync(imageView).execute();
                    } else {
                        imageIndex = imageLinks.size() - 1;
                        new GetImageAsync(imageView).execute();
                    }
                }
            }
        });

    }



    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            return false;
        }
        return true;
    }

    private void formatKeywords(String result){
        String[] keywordsArray = result.split(";");
        for(String str : keywordsArray){
            str.substring(0, str.length()-1);
            keywords.add(str);
            Log.d("Demo", str);
        }
    }

    private void formatImages(String result){
        String[] images = result.split("\n");
        if(imageLinks != null && imageLinks.isEmpty()) {
            for (String str : images) {
                imageLinks.add(str);
            }
        }

    }

    private void showPopup(ArrayList<String> keywords) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Choose Keyword");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        for(String str : keywords){
            arrayAdapter.add(str);
            Log.d("Keyword", str);
        }

        alert.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedKeyword = arrayAdapter.getItem(which);
                search.setText(selectedKeyword);

                RequestParams requestParams = new RequestParams();
                requestParams.addParameter("keyword", selectedKeyword);
                imageLinks = new ArrayList<>();
                new GetImageLinksAsync(requestParams).execute("http://dev.theappsdr.com/apis/photos/index.php");
                if(!imageLinks.isEmpty()) {
                    new GetImageAsync(imageView).execute();
                } else {
                    Toast toast = Toast.makeText(MainActivity.this,"No images returned!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                dialog.dismiss();
            }
        });

        alert.show();
    }

    private class GetDataUsingGetAsync extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            StringBuilder sb = new StringBuilder();
            HttpURLConnection connection = null;
            BufferedReader bufferedReader = null;
            String result = null;
            try {

                URL url = new URL("http://dev.theappsdr.com/apis/photos/keywords.php");
                connection = (HttpURLConnection) url.openConnection();
                bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                result = sb.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }

                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();

                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                }
            }


            formatKeywords(result);
            return result;
        }

        @Override
        protected  void onPostExecute(String result){
            if(result != null){
                Log.d("Demo", result);
            } else {
                Log.d("Demo", "NO RESULT!");
            }
        }
    }

    private class GetImageLinksAsync extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        RequestParams mParams;

        public GetImageLinksAsync(RequestParams params){
            mParams = params;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Loading Dictionary...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            StringBuilder sb = new StringBuilder();
            HttpURLConnection connection = null;
            BufferedReader bufferedReader = null;
            String result = null;
            try {

                URL url = new URL(mParams.getEncodedURL(params[0]));
                connection = (HttpURLConnection) url.openConnection();
                bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                result = sb.toString();
                formatImages(result);

            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }

                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();

                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                }
            }

            return result;
        }

        @Override
        protected  void onPostExecute(String result){
            if(result != null){
                Log.d("Demo", result);
            } else {
                Log.d("Demo", "NO RESULT!");
            }
            progressDialog.dismiss();
        }
    }

    private class GetImageAsync extends AsyncTask<String, Void, Bitmap>{

        ProgressDialog progressDialog;
        ImageView imageView;

        public GetImageAsync(ImageView imageView){
            this.imageView = imageView;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Loading Photo...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            StringBuilder sb = new StringBuilder();
            HttpURLConnection connection = null;
            Bitmap image = null;

            try {
                URL url = new URL(imageLinks.get(imageIndex));
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    image = BitmapFactory.decodeStream(connection.getInputStream());
                    return image;
                }

            } catch(MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(connection != null){
                    connection.disconnect();
                }
            }

            return image;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null && imageView != null){
                imageView.setImageBitmap(bitmap);
            }
            progressDialog.dismiss();
        }
    }

}

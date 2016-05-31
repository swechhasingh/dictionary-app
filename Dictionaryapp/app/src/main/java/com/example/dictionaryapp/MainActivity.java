package com.example.dictionaryapp;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button searchButton;
    EditText wordInput;
    private static final String  url0 = "http://api.wordnik.com:80/v4/word.json/";
    private static final String url1 = "/definitions?limit=200&includeRelated=true&useCanonical=false&includeTags=false&api_key=54d58ad1400c9bc39d59b4c5dae05829cb6d4086bcfe2c886";
    String url,response;
    int duration = Toast.LENGTH_SHORT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchButton = (Button) findViewById(R.id.button);
        wordInput = (EditText) findViewById(R.id.editText);

        searchButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        url = url0 + wordInput.getText() + url1;
        Log.i("url", url);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(this, url, null, "application/json", new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                String responsestring = new String(responseBody);
                Toast toast = Toast.makeText(getApplicationContext(), "success" + statusCode, duration);
                toast.show();
                Log.i("response", responsestring);
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                Toast toast = Toast.makeText(getApplicationContext(), "failed" + statusCode, duration);
                toast.show();
                Log.i("url", url);
            }
        });

//        DefaultHttpClient httpClient = new DefaultHttpClient();
//        HttpEntity httpEntity = null;
//        HttpResponse httpResponse = null;
//
//        HttpGet httpGet = new HttpGet(url);
//        try {
//            httpResponse = httpClient.execute(httpGet);
//            httpEntity = httpResponse.getEntity();
//            response = EntityUtils.toString(httpEntity);
//            Log.i("response", response);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Word Meaning");
        builder.setMessage(wordInput.getText());
        builder.setCancelable(true);

        builder.setNegativeButton(
                "Close",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

}

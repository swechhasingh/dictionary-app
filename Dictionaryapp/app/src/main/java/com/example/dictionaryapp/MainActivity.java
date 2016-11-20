package com.example.dictionaryapp;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    TextView textView;
    TextView resusltTxt;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        resusltTxt = (TextView) findViewById(R.id.meaning);

        GetWordOfTheDayTask getWordOfTheDayTask = new GetWordOfTheDayTask();
        getWordOfTheDayTask.execute();

        // Get the SearchView and set the searchable configuration
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) findViewById(R.id.searchView);
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        EditText txtSearch = ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text));
        txtSearch.setHintTextColor(Color.LTGRAY);
        txtSearch.setTextColor(Color.WHITE);

        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(txtSearch, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                Intent intent = new Intent(getApplicationContext(), SearchResultActivity.class);
                intent.putExtra("query", query);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });
    }

    public class GetWordOfTheDayTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = MainActivity.GetWordOfTheDayTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            String JsonStr = null;

            try {

                final String BASE_URL1 = "http://api.wordnik.com:80/v4/words.json/wordOfTheDay?date=";
                final String BASE_URL2 = "&api_key=54d58ad1400c9bc39d59b4c5dae05829cb6d4086bcfe2c886";
                final String BASE_URL = BASE_URL1 + date + BASE_URL2;

                URL url = new URL(BASE_URL);

                Log.v(LOG_TAG, "URi"+ BASE_URL);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                JsonStr = buffer.toString();

                Log.v(LOG_TAG,"JSON String: "+ JsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return JsonStr;
        }

        @Override
        protected void onPostExecute(String JsonStr) {
            if(JsonStr != null){

                final String WORD_MEANING = "text";
                final String PART_OF_SPEECH = "partOfSpeech";
                String resultStr = "No word found.";
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(JsonStr);
                    textView.setText(jsonObject.getString("word"));
                    JSONArray jsonArray = jsonObject.getJSONArray("definitions");
                    JSONObject jsonObj = jsonArray.getJSONObject(0);
                    resultStr = PART_OF_SPEECH + " : " +jsonObj.getString(PART_OF_SPEECH) + "\n\n" + "Meaning : \n";
                    for (int i = 0; i < jsonArray.length(); i++){
                        JSONObject jsonOBJECT = jsonArray.getJSONObject(i);
                        resultStr = resultStr + jsonOBJECT.getString(WORD_MEANING) + "\n";
                    }

                    jsonArray = jsonObject.getJSONArray("examples");
                    resultStr = resultStr + "\nExamples" + "\n";
                    for (int i = 0; i < jsonArray.length(); i++){
                        JSONObject jsonOBJECT = jsonArray.getJSONObject(i);
                        int j = i+1;
                        resultStr = resultStr + j + ". " +jsonOBJECT.getString("text") + "\n";
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.v(LOG_TAG, "Word meaning: " + resultStr);
                resusltTxt.setText(resultStr);
            }

        }
    }


}

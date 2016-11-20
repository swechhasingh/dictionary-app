package com.example.dictionaryapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SearchResultActivity extends AppCompatActivity {

    TextView wordTxt, meaningTxt;
    String word;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_result);

        wordTxt = (TextView) findViewById(R.id.word);
        meaningTxt = (TextView) findViewById(R.id.meaning);

        Intent intent = getIntent();
        word = intent.getStringExtra("query");
        wordTxt.setText(word);
        GetWordMeaningTask getWordMeaningTask = new GetWordMeaningTask();
        getWordMeaningTask.execute(word.trim().toLowerCase());

    }

    public class GetWordMeaningTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = SearchResultActivity.GetWordMeaningTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String JsonStr = null;

            try {

                final String BASE_URL1 = "http://api.wordnik.com:80/v4/word.json/";
                final String BASE_URL2 = "/definitions?limit=200&includeRelated=true&useCanonical=false&includeTags=false&api_key=54d58ad1400c9bc39d59b4c5dae05829cb6d4086bcfe2c886";
                final String BASE_URL = BASE_URL1 + params[0] + BASE_URL2;

//                final String EX_API_URL = "http://api.wordnik.com:80/v4/word.json/";
//                final String EX_API_URL1 = "/topExample?useCanonical=false&api_key=54d58ad1400c9bc39d59b4c5dae05829cb6d4086bcfe2c886";
//                final String EX_URL = EX_API_URL + params[0] + EX_API_URL1;

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
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray(JsonStr);
                    if(jsonArray.length() <= 0) {
                        meaningTxt.setText(resultStr);
                        return;
                    }
                    JSONObject jsonObj = jsonArray.getJSONObject(0);
                    resultStr = PART_OF_SPEECH + " : " +jsonObj.getString(PART_OF_SPEECH) + "\n\n" + "Meaning : \n";
                    for (int i = 0; i < jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        resultStr = resultStr + jsonObject.getString(WORD_MEANING) + "\n";
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.v(LOG_TAG, "Word meaning: " + resultStr);
                meaningTxt.setText(resultStr);
            }

        }
    }

}

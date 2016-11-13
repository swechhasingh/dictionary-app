package com.example.dictionaryapp;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import static com.example.dictionaryapp.R.id.textView;

/**
 * Created by swechha on 11/12/16.
 */
public class SearchResultsActivity extends Activity {

    String query;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.search_result);
        TextView textView = (TextView) findViewById(R.id.textView);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, "Searching by: "+ query, Toast.LENGTH_SHORT).show();
            GetWordMeanTask getWordMeanTask = new GetWordMeanTask();
            getWordMeanTask.execute(query.trim().toLowerCase());
        }
    }

    public class GetWordMeanTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = MainActivity.GetWordMeanTask.class.getSimpleName();

        private String getWordMeaningFromJson(String JsonStr)
                throws JSONException {

            final String WORD_MEANING = "text";
            String resultStr = "No word found.";
            JSONArray jsonArray = new JSONArray(JsonStr);
            if(jsonArray.length() > 0) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                resultStr = jsonObject.getString(WORD_MEANING);
            }

            Log.v(LOG_TAG, "Word meaning: " + resultStr);

            return resultStr;

        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String JsonStr = null;

            try {

                final String BASE_URL1 = "http://api.wordnik.com:80/v4/word.json/";
                final String BASE_URL2 = "/definitions?limit=200&includeRelated=true&useCanonical=false&includeTags=false&api_key=54d58ad1400c9bc39d59b4c5dae05829cb6d4086bcfe2c886";
                final String BASE_URL = BASE_URL1 + params[0] + BASE_URL2;

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
            try {
                return getWordMeaningFromJson(JsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String string) {
            if(string != null){
                System.out.println(string);
                //textView.setText(query + ": " + string);
//                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
//                builder.setTitle("Word Meaning");
//                builder.setMessage(wordInput.getText() + string);
//                builder.setCancelable(true);
//
//                builder.setNegativeButton(
//                        "Close",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        });
//
//                AlertDialog alert = builder.create();
//                alert.show();
            }

        }
    }

}

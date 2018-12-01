package com.bitsanddroids.newsfeed;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    ArrayList<String> articleTitles;
    static ArrayList<String> articleURLS;
    ListView titlesListView;
    ArrayAdapter<String> arrayAdapter;
    SQLiteDatabase articles;
    //https://hacker-news.firebaseio.com/v0/item/126809.json?print=pretty  126809=ARTICLE ID RETURNS JSON DATA

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        articleTitles = new ArrayList<>();
        articleURLS = new ArrayList<>();


        articles = this.openOrCreateDatabase("Articles", MODE_PRIVATE, null);
        articles.execSQL("CREATE TABLE IF NOT EXISTS newArticles (title VARCHAR, url VARCHAR, articleID VARCHAR)");

        //delete content of table to avoid piling of articles
        try {
            articles.execSQL("DELETE FROM newArticles");
        } catch (Exception e) {
            e.printStackTrace();
        }


        DownloadTask task = new DownloadTask();
        try {
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");

        } catch (Exception e) {
            e.printStackTrace();
        }
        titlesListView = findViewById(R.id.articlesListview);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, articleTitles);
        titlesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), articleBrowser.class);
                intent.putExtra("url", articleURLS.get(position));
                startActivity(intent);
            }
        });


    }


    protected class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection httpURLConnection;

            //download JSONArray from API
            try {
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                int data = inputStreamReader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = inputStreamReader.read();
                }

                JSONArray jsonArray = new JSONArray(result);

                int numberOfItems = 20;

                if (jsonArray.length() < 20) {
                    numberOfItems = jsonArray.length();
                }

                //download article info from the article ID's
                for (int i = 0; i < numberOfItems + 1; i++) {
                    String articleId = jsonArray.getString(i);
                    String articleInfo = "";
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/" + articleId + ".json?print=pretty");
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    in = httpURLConnection.getInputStream();
                    inputStreamReader = new InputStreamReader(in);
                    data = inputStreamReader.read();
                    while (data != -1) {
                        char current = (char) data;
                        articleInfo += current;
                        data = inputStreamReader.read();
                    }
                    //convert downloaded info to JSONObject

                    JSONObject jsonObject = new JSONObject(articleInfo);

                    String title = jsonObject.getString("title");
                    title = title.replaceAll("'", "''");
                    String articleUrl = jsonObject.getString("url");

                    String selectSQL = "INSERT INTO newArticles(title, url, articleID) VALUES ('" + title + "','" + articleUrl + "','" + articleId + "')";


                    articles.execSQL(selectSQL);


                }

                Cursor cursor = articles.rawQuery("SELECT * FROM newArticles", null);
                int titleIndex = cursor.getColumnIndex("title");
                int urlIndex = cursor.getColumnIndex("url");

                cursor.moveToFirst();
                // move trough SQLite Table to add article info
                while (cursor != null) {

                    articleTitles.add(cursor.getString(titleIndex));
                    articleURLS.add(cursor.getString(urlIndex));

                    cursor.moveToNext();
                }
                cursor.close();


            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //put adapter to display the titles
            titlesListView.setAdapter(arrayAdapter);


        }
    }
}

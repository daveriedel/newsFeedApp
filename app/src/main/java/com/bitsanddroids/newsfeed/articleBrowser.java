package com.bitsanddroids.newsfeed;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;



public class articleBrowser extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_browser);

        Intent intent = getIntent();

         String url = intent.getStringExtra("url");


        WebView webView = findViewById(R.id.articleWebView);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);


        webView.loadUrl(url);




    }



}

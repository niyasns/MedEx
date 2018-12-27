package com.niyas.android.medex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PdfViewer extends AppCompatActivity {

    private static final String TAG = "PdfViewer";
    WebView webView;
    PDFView pdfView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setContentView(R.layout.activity_pdf_viewer);

        Intent intent = getIntent();
        File file = (File) intent.getSerializableExtra("file");
        String url = (String) intent.getStringExtra("url");

/*        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        String imageData = null;

        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read = 0;
            while ((read = ios.read(buffer)) != -1 ) {
                ous.write(buffer, 0, read);
            }
            imageData = Base64.encodeToString(ous.toByteArray(), Base64.DEFAULT)
                    .replace("\n", "").replace("\r","");
        } catch (Exception e) {
            e.printStackTrace();
        }*/

   /*   webView = findViewById(R.id.fullscreen_web_view);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setBuiltInZoomControls(false);
        webView.setWebChromeClient(new WebChromeClient());*/
        /*final String finalImageData = imageData;*/
   /*     webView.setWebViewClient(new WebViewClient(){
            private boolean loaded = false;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loaded = true;
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(loaded) {
                    loadPdf(finalImageData);<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
                    loaded = false;
                }
            }
        });*/
        pdfView = (PDFView) findViewById(R.id.fullscreen_web_view);
        try{
            pdfView.fromFile(file)
                    .defaultPage(1)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .spacing(4)
                    .enableAnnotationRendering(true)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .load();
        } catch (Exception e) {
            Toast.makeText(this, "Pdf loading failed", Toast.LENGTH_SHORT).show();
        }
    }

/*
    private void loadPdf(String imageData) {

        String data = "data:application/pdf;base64," + imageData;
        String javascript = "javascript:(function() { loadPDF('" + data + "'); })()";
        webView.loadUrl(javascript);
    }
*/

 /*   private static String base64Encode(FileInputStream is) {

        StringBuilder encStr = new StringBuilder();
        try {
            int bytesRead = 0;
            int chunkSize = 10000000;
            byte[] chunk = new byte[chunkSize];
            while ((bytesRead = is.read(chunk)) > 0) {

                byte[] ba = new byte[bytesRead];

                for(int i = 0; i < ba.length; i++) {
                    ba[i] = chunk[i];
                }
                encStr.append(Base64.encodeToString(ba, Base64.DEFAULT | Base64.NO_PADDING | Base64.NO_WRAP));

            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return String.valueOf(encStr);
    }

    // convert InputStream to String
    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }*/
}

package com.niyas.android.medex;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PrivacyFragment extends android.support.v4.app.Fragment {

    private View parentView;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_privacy, container, false);
        setupViews();
        return parentView;
    }

    private void setupViews() {

        HomeActivity parentActivity = (HomeActivity) getActivity();
        Typeface raleway_bold = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Bold.ttf" );
        TextView heading = parentActivity.findViewById(R.id.heading);
        progressBar = parentActivity.findViewById(R.id.progressbarHome);
        progressBar.setVisibility(View.VISIBLE);
        heading.setText("Privacy Policy");
        heading.setTypeface(raleway_bold);
        WebView privacy = parentView.findViewById(R.id.privacy_policy_web_view);
        privacy.loadUrl("https://www.freeprivacypolicy.com/privacy/view/02b78778cc64beb5b677d3f5bad95c79");
        privacy.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        privacy.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}

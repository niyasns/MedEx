package com.example.android.medex;


import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static java.security.AccessController.getContext;

public class SliderAdapter extends PagerAdapter{

    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapter(Context context) {
        this.context = context;
    }

    /*Slider images */
    public int[] slide_images = {
            R.drawable.logo_launch,
            R.drawable.think,
            R.drawable.docs
    };
    /* Slider headings */
    public String[] slide_headings = {
            "PRATITI",
            "VIDEOS",
            "DOC REPO"
    };
    /* Slider sub headings */
    public String[] slide_descs = {
            "An application by \nSFI Thiruvananthapuram \nMedical College Unit",
            "Watch videos for better understanding",
            "Download study modules effortlessly",
    };

    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (RelativeLayout) object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        if(layoutInflater == null) {
            Log.d("SliderAdapter", "LayoutInflator error");
        }
        View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

        ImageView slideImageView = (ImageView) view.findViewById(R.id.slide_pic);
        TextView slideHeading = (TextView) view.findViewById(R.id.slide_heading);
        TextView slideDescription = (TextView) view.findViewById(R.id.slide_text);

        slideImageView.setImageResource(slide_images[position]);
        slideHeading.setText(slide_headings[position]);
        slideDescription.setText(slide_descs[position]);

        slideHeading.setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/Raleway-Bold.ttf" ));
        slideDescription.setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/Raleway-Regular.ttf" ));

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((RelativeLayout)object);
    }
}

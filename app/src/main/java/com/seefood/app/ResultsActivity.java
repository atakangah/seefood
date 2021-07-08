package com.seefood.app;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.seefood.app.Adapter.PagerAdapter;
import com.seefood.app.fragment.pagerFragments.MealFragment;
import com.seefood.app.fragment.pagerFragments.HealthFragment;

public class ResultsActivity extends AppCompatActivity {

    private String TAG = "DEBUG";

    private ViewPager mViewPager;
    private RelativeLayout resultHolder;

    private ImageView overviewBtn;
    private ImageView treatmentBtn;
    private ImageView reScanBtn;

    private TextView overviewTxt;
    private TextView treatmentTxt;
    private TextView reScanTxt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_results);

        resultHolder = findViewById(R.id.resultHolder);
        mViewPager = findViewById(R.id.viewPager);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        /*
         * Setup Navigation Buttons For The Results Screen
         * Set Viewpager To Scroll To Overview
         * Set Viewpager To Scroll To Treatments Tab
         * Go Back One Screen TO The Camera View
         */
        overviewBtn = findViewById(R.id.v1);
        treatmentBtn = findViewById(R.id.v2);
        reScanBtn = findViewById(R.id.v3);

        overviewTxt = findViewById(R.id.v1t);
        treatmentTxt = findViewById(R.id.v2t);
        reScanTxt = findViewById(R.id.v3t);

        overviewBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mViewPager.setCurrentItem(0);
                animateHighlight(overviewBtn, overviewTxt);
                animateUnhighlight(treatmentBtn, treatmentTxt);
            }
        });
        treatmentBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mViewPager.setCurrentItem(1);
                animateHighlight(treatmentBtn, treatmentTxt);
                animateUnhighlight(overviewBtn, overviewTxt);
            }
        });
        reScanBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                animateHighlight(reScanBtn, reScanTxt);
                onBackPressed();
            }
        });

        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new MealFragment(), "Your Meal");
        pagerAdapter.addFragment(new HealthFragment(), "Your Health");

        mViewPager.setAdapter(pagerAdapter);

        initialize();
    }


    private void initialize() {
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        layoutParams.setLayoutDirection(LinearLayout.VERTICAL);
//        layoutParams.leftMargin = 50;
//        LinearLayout layoutHolder = new LinearLayout(getApplicationContext());
//        layoutHolder.setOrientation(LinearLayout.VERTICAL);
//        layoutHolder.setLayoutParams(layoutParams);
//        while (it.hasNext()) {
//        TextView resTextV = new TextView(getApplicationContext());
//        Recognition r = it.next();
//        resTextV.setText(r.getTitle() + ":    " + r.getConfidence().toString());
//        resTextV.setTextColor(Color.WHITE);
//        resTextV.setTextSize(14);
//        resTextV.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        Log.d(TAG, "item");
//        layoutHolder.addView(resTextV);

        /**
         * Overview Tab is the first tab shown when @ResultsActivity is started
         * Hence, animate overview button to be highlighted
         */
        animateHighlight(overviewBtn, overviewTxt);
        overviewTxt.setTextAppearance(R.style.textWhite);
        overviewTxt.invalidate();
    }


    private void animateHighlight(ImageView target, final TextView target2) {
        ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(target,
                PropertyValuesHolder.ofFloat("scaleX", 1.19f),
                PropertyValuesHolder.ofFloat("scaleY", 1.19f)
        );
        scaleAnimator.setDuration(300);
        scaleAnimator.start();
        ObjectAnimator scaleAnimator2 = ObjectAnimator.ofPropertyValuesHolder(target2,
                PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                PropertyValuesHolder.ofFloat("scaleY", 1.2f)
        );
        scaleAnimator2.setDuration(300);
        scaleAnimator2.start();
    }


    private void animateUnhighlight(ImageView target, final TextView target2) {
        ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(target,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f)
        );
        scaleAnimator.setDuration(300);
        scaleAnimator.start();
        ObjectAnimator scaleAnimator2 = ObjectAnimator.ofPropertyValuesHolder(target2,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f)
        );
        scaleAnimator2.setDuration(300);
        scaleAnimator2.start();
    }

}

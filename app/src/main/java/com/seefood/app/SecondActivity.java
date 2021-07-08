package com.seefood.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.view.View;
import android.widget.TextView;


import com.seefood.app.Adapter.Adapter_walkthrough;
import me.relex.circleindicator.CircleIndicator;

public class SecondActivity extends AppCompatActivity {

    public ViewPager viewpager;

    Adapter_walkthrough adapter_walkthrough;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_two);

        TextView mTextView = findViewById(R.id.fragLabel);

        viewpager = findViewById(R.id.viewpager);

        CircleIndicator indicator = findViewById(R.id.indicator);

        adapter_walkthrough = new Adapter_walkthrough(getSupportFragmentManager(), mTextView);

        viewpager.setAdapter(adapter_walkthrough);

        indicator.setViewPager(viewpager);

        adapter_walkthrough.registerDataSetObserver(indicator.getDataSetObserver());



        /* CUSTOM CODE */
        TextView nextBtn = findViewById(R.id.tv_thirdnextstep);
        nextBtn.setOnClickListener(new ViewPager.OnClickListener() {
            public void onClick(View v) {
                Intent act3Intent = new Intent(SecondActivity.this, ThirdActivity.class);
                startActivity(act3Intent);
            }
        });
    }
}
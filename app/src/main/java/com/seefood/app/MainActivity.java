package com.seefood.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.view.View;

import com.seefood.app.Adapter.Adapter_walkthrough;
import com.seefood.app.utilities.JSONParser;
import com.seefood.app.utilities.Messenger;

import me.relex.circleindicator.CircleIndicator;

public class MainActivity extends AppCompatActivity {

    public ViewPager viewpager;
    public Adapter_walkthrough adapter_walkthrough;

    private int REQUEST_CODE = 100;
    private String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewpager = findViewById(R.id.viewpager);

        CircleIndicator indicator = findViewById(R.id.indicator);

        adapter_walkthrough = new Adapter_walkthrough(getSupportFragmentManager(), null);

        viewpager.setAdapter(adapter_walkthrough);

        indicator.setViewPager(viewpager);

        adapter_walkthrough.registerDataSetObserver(indicator.getDataSetObserver());

        Messenger.setSeefoodNutrition(JSONParser.parseJSON(getApplicationContext()));
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!allPermitted()) {
            doRequestPermission();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 1) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    return;
                else
                    doRequestPermission();
            }
        }
    }


    private void doRequestPermission() {
        requestPermissions(new String[] { PERMISSION_CAMERA, PERMISSION_STORAGE }, REQUEST_CODE);
    }

    private boolean allPermitted () {
        return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(PERMISSION_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void getStarted(View view) {
        Intent act3Intent = new Intent(MainActivity.this, CameraActivity.class);
        startActivity(act3Intent);
    }

}

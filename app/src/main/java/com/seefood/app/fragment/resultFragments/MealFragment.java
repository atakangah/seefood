package com.seefood.app.fragment.resultFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.seefood.app.R;
import com.seefood.app.models.Recognition;
import com.seefood.app.CameraActivity;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

public class MealFragment extends Fragment {
    private View mOverviewLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mOverviewLayout = inflater.inflate(R.layout.meal_fragment, container, false);
        final File sourceFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "tensorflow_preview.png");
        ImageView snappedImgView = mOverviewLayout.findViewById(R.id.snapped_img);

        Glide.with(mOverviewLayout.getContext())
                .load(sourceFile)
                .centerCrop()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(snappedImgView);

        setRecognitionResult();

        return mOverviewLayout;
    }

    public void setRecognitionResult() {
        Collection<Recognition> mRecognition = CameraActivity.mRecognitionResults;

        if (mRecognition == null || mRecognition.isEmpty())
            return;

        Iterator<Recognition> it = mRecognition.iterator();
        Recognition recognitionTopResult = it.next();

        while (it.hasNext())
            Log.d("DEBUG", "result: "+it.next().getTitle());

        TextView recognitionResultTextView = mOverviewLayout.findViewById(R.id.recognition_result_text);
        recognitionResultTextView.setText(recognitionTopResult.getTitle().replaceFirst("\\d", ""));
    }

}

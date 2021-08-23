package com.seefood.app.fragment.resultFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.seefood.app.CameraActivity;
import com.seefood.app.MainActivity;
import com.seefood.app.R;
import com.seefood.app.ResultsActivity;
import com.seefood.app.models.Nutrition;
import com.seefood.app.models.Recognition;
import com.seefood.app.utilities.CameraHandler;
import com.seefood.app.utilities.Messenger;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class HealthFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layoutView = inflater.inflate(R.layout.health_fragment, container, false);

        initialize(layoutView);

        return layoutView;
    }

    private void initialize(View layoutView) {
        TextView ingredientsTextView = layoutView.findViewById(R.id.ingredients);
        TextView benefitsTextView = layoutView.findViewById(R.id.healthBenefits);
        TextView healthAdviceTextView = layoutView.findViewById(R.id.healthAdvice);

        Collection<Recognition> mRecognition = Messenger.getRecognitionResults();

        if (mRecognition == null || mRecognition.isEmpty()) return;

        Iterator<Recognition> it = mRecognition.iterator();
        Recognition recognitionTopResult = it.next();


        HashMap<Integer, Nutrition> seefoodNutrition = Messenger.getSeefoodNutrition();
        int nutritionIndex = Integer.parseInt(String.valueOf(recognitionTopResult.getTitle().charAt(0)));
        Nutrition nutrition = seefoodNutrition.get(0);

        healthAdviceTextView.setText(nutrition.getHealthAdvice());

        StringBuilder healthBenefitsTxt = new StringBuilder();
        for (String benefit : nutrition.getHealthBenefits()) {
            healthBenefitsTxt.append(benefit).append(", ");
        }
        benefitsTextView.setText(healthBenefitsTxt);

        StringBuilder ingredientsTxt = new StringBuilder();
        for (String ingredient : nutrition.getIngredients()) {
            ingredientsTxt.append(ingredient).append(", ");
        }
        ingredientsTextView.setText(ingredientsTxt);
    }
}

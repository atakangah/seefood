package com.seefood.app.utilities;

import com.seefood.app.models.Nutrition;
import com.seefood.app.models.Recognition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Messenger {
    private static Collection<Recognition> mRecognitionResults;
    private static HashMap<Integer, Nutrition> mSeefoodNutrition;

    public static Collection<Recognition> getRecognitionResults() {
        return mRecognitionResults;
    }

    public static void setRecognitionResults(Collection<Recognition> recognitionResults) {
        mRecognitionResults = recognitionResults;
    }

    public static HashMap<Integer, Nutrition> getSeefoodNutrition() {
        return mSeefoodNutrition;
    }

    public static void setSeefoodNutrition(HashMap<Integer, Nutrition> seefoodNutrition) {
        mSeefoodNutrition = seefoodNutrition;
    }
}

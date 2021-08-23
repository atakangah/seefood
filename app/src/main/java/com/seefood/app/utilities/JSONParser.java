package com.seefood.app.utilities;

import android.content.Context;
import android.util.Log;

import com.seefood.app.models.Nutrition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class JSONParser {

    public static String loadJSONFromAsset(Context appContext) {
        String json = null;
        try {
            InputStream is = appContext.getApplicationContext().getAssets().open("seefoodnutriton.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private static ArrayList<String> parseArrayList(JSONArray jsonArray) throws JSONException {
        ArrayList<String> resultArray = new ArrayList<>();
        for (int x = 0; x < jsonArray.length(); x++)
            resultArray.add(jsonArray.getString(x));

        return resultArray;
    }

    public static HashMap<Integer, Nutrition> parseJSON(Context appContext) {
        try {
            JSONArray nutritionArray = new JSONArray(loadJSONFromAsset(appContext));
            HashMap<Integer, Nutrition> nutritionMap = new HashMap();

            for (int i = 0; i < nutritionArray.length(); i++) {
                JSONObject nutritionObject = nutritionArray.getJSONObject(i);

                int key = Integer.parseInt(nutritionObject.getString("key"));
                String healthAdvice = nutritionObject.getString("healthAdvice");
                ArrayList<String> ingredients = parseArrayList(nutritionObject.getJSONArray("ingredients"));
                ArrayList<String> healthBenefits = parseArrayList(nutritionObject.getJSONArray("benefits"));
                Log.d("DEBUG", "health all ====>" + healthAdvice);

                Nutrition nutrition = new Nutrition();
                nutrition.setKey(key);
                nutrition.setHealthAdvice(healthAdvice);
                nutrition.setHealthBenefits(healthBenefits);
                nutrition.setIngredients(ingredients);

                nutritionMap.put(key, nutrition);
            }

            return nutritionMap;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("DEBUG", "Error"+e.getMessage());
        }
        return null;
    }
}

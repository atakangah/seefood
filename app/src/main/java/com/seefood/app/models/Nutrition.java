package com.seefood.app.models;

import java.util.ArrayList;

public class Nutrition {
    private ArrayList<String> ingredients;
    private ArrayList<String> healthBenefits;
    private String healthAdvice;
    private int key;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
    }

    public ArrayList<String> getHealthBenefits() {
        return healthBenefits;
    }

    public void setHealthBenefits(ArrayList<String> healthBenefits) {
        this.healthBenefits = healthBenefits;
    }

    public String getHealthAdvice() {
        return healthAdvice;
    }

    public void setHealthAdvice(String healthAdvice) {
        this.healthAdvice = healthAdvice;
    }
}

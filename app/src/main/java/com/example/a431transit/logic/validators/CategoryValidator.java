package com.example.a431transit.logic.validators;

public class CategoryValidator {
    public static boolean validateCategoryName(String name) {
        return name != null && !name.isEmpty();
    }
}

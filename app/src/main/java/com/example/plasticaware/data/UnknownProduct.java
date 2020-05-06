package com.example.plasticaware.data;

public class UnknownProduct {
    private String title;
    private String description;

    public UnknownProduct() {
        //empty constructor needed
    }

    public UnknownProduct(String title, String description) {
        this.title = title;
        this.description = description;

    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

}

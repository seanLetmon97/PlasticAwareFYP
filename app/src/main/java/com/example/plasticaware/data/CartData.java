package com.example.plasticaware.data;


public class CartData  {

    private String title;
    private String description;
    private int score;
    private String image;
    private  int quantity;
    public CartData() {
        //empty constructor needed
    }
    public CartData(String title,String description,int score,String image) {
        this.title = title;
        this.description = description;
        this.score = score;
        this.image=image;

    }

    public CartData(String title,String description,int score,String image,int quantity) {
        this.title = title;
        this.description = description;
        this.score = score;
        this.image=image;
        this.quantity=quantity;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getScore() {
        return score;
    }


    public String getImage() {
        return image;
    }

    public int getQuantity() {
        return quantity;
    }

}
package com.example.plasticaware.data;

import java.io.Serializable;

public class Product implements Serializable {
  private String title;
  private String description;
  private int score;
  private String group;
  private String image;


  public Product() {
    //empty constructor needed
  }

  public Product(String title, String description, int priority,String group,String image) {
    this.title = title;
    this.description = description;
    this.score = priority;
    this.group=group;
    this.image=image;
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

  public String getGroup() {
    return group;
  }

  public String getImage() { return image; }
}
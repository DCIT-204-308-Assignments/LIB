package com.group2.model;

public class FoodItem {

    private final String id;
    private final String name;
    private final double price;
    private final String restaurantId;

    public FoodItem(String id, String name, double price, String restaurantId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.restaurantId = restaurantId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    @Override
    public String toString() {
        return "FoodItem{id='" + id + "', name='" + name + "', price=" + price + ", restaurantId='" + restaurantId + "'}";
    }
}

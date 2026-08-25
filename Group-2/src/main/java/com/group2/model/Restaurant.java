package com.group2.model;

public class Restaurant implements Rated {

    private final String id;
    private final String name;
    private final String location;
    private final double rating;

    public Restaurant(String id, String name, String location, double rating) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public double getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return "Restaurant{id='" + id + "', name='" + name + "', location='" + location + "', rating=" + rating + "}";
    }
}

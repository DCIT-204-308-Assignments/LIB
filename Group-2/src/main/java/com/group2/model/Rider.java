package com.group2.model;

public class Rider implements Rated {

    private final String id;
    private final String name;
    private final boolean available;
    private final double rating;

    public Rider(String id, String name, boolean available, double rating) {
        this.id = id;
        this.name = name;
        this.available = available;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isAvailable() {
        return available;
    }

    @Override
    public double getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return "Rider{id='" + id + "', name='" + name + "', available=" + available + ", rating=" + rating + "}";
    }
}

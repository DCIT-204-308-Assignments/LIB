package com.group2.sorting;

import com.group2.model.Restaurant;
import com.group2.model.Rider;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RatingSortServiceTest {

    @Test
    void sortByRating_ordersRestaurantsAscendingByRating() {
        List<Restaurant> restaurants = new ArrayList<>(List.of(
                new Restaurant("REST1", "Balme Bites", "Balme", 4.5),
                new Restaurant("REST2", "Commonwealth Grill", "Commonwealth", 3.2),
                new Restaurant("REST3", "Legon Eats", "Legon", 4.9)
        ));

        RatingSortService.sortByRating(restaurants, QuickSort::sort);

        assertEquals(List.of("REST2", "REST1", "REST3"),
                restaurants.stream().map(Restaurant::getId).toList());
    }

    @Test
    void sortByRating_ordersRidersAscendingByRating() {
        List<Rider> riders = new ArrayList<>(List.of(
                new Rider("RID1", "Ama", true, 4.8),
                new Rider("RID2", "Kojo", false, 4.0),
                new Rider("RID3", "Esi", true, 4.95)
        ));

        RatingSortService.sortByRating(riders, QuickSort::sort);

        assertEquals(List.of("RID2", "RID1", "RID3"),
                riders.stream().map(Rider::getId).toList());
    }
}

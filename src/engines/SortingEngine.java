package engines;

import java.util.Comparator;
import ds.DynamicArray;
import ds.HashTable;
import models.Order;
import models.Resource;

public class SortingEngine {

    // ---- SEARCHING ----

    /**
     * Linear Search: works on any array.
     */
    public static <T> int linearSearch(DynamicArray<T> list, T target, Comparator<? super T> comp) {
        for (int i = 0; i < list.size(); i++) {
            if (comp.compare(list.get(i), target) == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Binary Search: Precondition: list must be sorted in ascending order.
     */
    public static <T> int binarySearch(DynamicArray<T> list, T target, Comparator<? super T> comp) {
        // Precondition verification: Check if array is indeed sorted!
        if (!isSorted(list, comp)) {
            throw new IllegalStateException("Precondition failed: Binary Search requires a sorted list.");
        }

        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            T midVal = list.get(mid);
            int cmp = comp.compare(midVal, target);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid; // Key found
            }
        }
        return -1; // Key not found
    }

    // ---- DELIVERY-SPECIFIC SEARCHING ----

public static int linearSearchOrderById(
        DynamicArray<Order> orders,
        int orderId) {

    if (orders == null || orderId < 0) {
        return -1;
    }

    for (int i = 0; i < orders.size(); i++) {
        Order order = orders.get(i);

        if (order != null && order.getOrderId() == orderId) {
            return i;
        }
    }

    return -1;
}

public static int binarySearchOrderById(
        DynamicArray<Order> orders,
        int orderId) {

    if (orders == null || orderId < 0) {
        return -1;
    }

    for (int i = 0; i < orders.size() - 1; i++) {
        Order current = orders.get(i);
        Order next = orders.get(i + 1);

        if (current == null || next == null
                || current.getOrderId() > next.getOrderId()) {
            throw new IllegalStateException(
                    "Binary Search requires orders sorted by order ID."
            );
        }
    }

    int low = 0;
    int high = orders.size() - 1;

    while (low <= high) {
        int mid = low + (high - low) / 2;
        Order order = orders.get(mid);

        if (order == null) {
            return -1;
        }

        if (order.getOrderId() == orderId) {
            return mid;
        }

        if (order.getOrderId() < orderId) {
            low = mid + 1;
        } else {
            high = mid - 1;
        }
    }

    return -1;
}

public static int linearSearchRiderById(
        DynamicArray<Resource> riders,
        int riderId) {

    if (riders == null || riderId < 0) {
        return -1;
    }

    for (int i = 0; i < riders.size(); i++) {
        Resource rider = riders.get(i);

        if (rider != null && rider.getResourceId() == riderId) {
            return i;
        }
    }

    return -1;
}

public static DynamicArray<Order> searchOrdersByCustomer(
        DynamicArray<Order> orders,
        String customerName) {

    return searchOrdersByText(
            orders,
            customerName,
            1
    );
}

public static DynamicArray<Order> searchOrdersByRestaurant(
        DynamicArray<Order> orders,
        String restaurant) {

    return searchOrdersByText(
            orders,
            restaurant,
            2
    );
}

public static DynamicArray<Order> searchOrdersByFoodItem(
        DynamicArray<Order> orders,
        String foodItem) {

    return searchOrdersByText(
            orders,
            foodItem,
            3
    );
}

private static DynamicArray<Order> searchOrdersByText(
        DynamicArray<Order> orders,
        String query,
        int field) {

    DynamicArray<Order> matches = new DynamicArray<>();

    if (orders == null || query == null) {
        return matches;
    }

    String target = query.trim();

    if (target.isEmpty()) {
        return matches;
    }

    for (Order order : orders) {
        if (order == null) {
            continue;
        }

        String value;

        if (field == 1) {
            value = order.getCustomerName();
        } else if (field == 2) {
            value = order.getRestaurant();
        } else if (field == 3) {
            value = order.getFoodItem();
        } else {
            continue;
        }

        if (value != null && value.equalsIgnoreCase(target)) {
            matches.add(order);
        }
    }

    return matches;
}

    public static <T> boolean isSorted(DynamicArray<T> list, Comparator<? super T> comp) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (comp.compare(list.get(i), list.get(i + 1)) > 0) {
                return false;
            }
        }
        return true;
    }

    // ---- SORTING ----

    /**
     * Selection Sort: $O(n^2)$ time, in-place, unstable.
     */
    public static <T> void selectionSort(DynamicArray<T> list, Comparator<? super T> comp) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (comp.compare(list.get(j), list.get(minIdx)) < 0) {
                    minIdx = j;
                }
            }
            swap(list, i, minIdx);
        }
    }

    /**
     * Insertion Sort: $O(n^2)$ time, in-place, stable.
     */
    public static <T> void insertionSort(DynamicArray<T> list, Comparator<? super T> comp) {
        int n = list.size();
        for (int i = 1; i < n; i++) {
            T key = list.get(i);
            int j = i - 1;
            while (j >= 0 && comp.compare(list.get(j), key) > 0) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }

    /**
     * Merge Sort: $O(n \log n)$ time, stable, out-of-place.
     */
    public static <T> void mergeSort(DynamicArray<T> list, Comparator<? super T> comp) {
        if (list.size() <= 1) return;
        mergeSortRec(list, 0, list.size() - 1, comp);
    }

    private static <T> void mergeSortRec(DynamicArray<T> list, int left, int right, Comparator<? super T> comp) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSortRec(list, left, mid, comp);
            mergeSortRec(list, mid + 1, right, comp);
            merge(list, left, mid, right, comp);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void merge(DynamicArray<T> list, int left, int mid, int right, Comparator<? super T> comp) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        Object[] L = new Object[n1];
        Object[] R = new Object[n2];

        for (int i = 0; i < n1; i++) L[i] = list.get(left + i);
        for (int j = 0; j < n2; j++) R[j] = list.get(mid + 1 + j);

        int i = 0, j = 0;
        int k = left;
        while (i < n1 && j < n2) {
            if (comp.compare((T) L[i], (T) R[j]) <= 0) {
                list.set(k, (T) L[i]);
                i++;
            } else {
                list.set(k, (T) R[j]);
                j++;
            }
            k++;
        }

        while (i < n1) {
            list.set(k, (T) L[i]);
            i++;
            k++;
        }

        while (j < n2) {
            list.set(k, (T) R[j]);
            j++;
            k++;
        }
    }

    /**
     * Quicksort: $O(n \log n)$ average time ($O(n^2)$ worst), in-place, unstable.
     */
    public static <T> void quickSort(DynamicArray<T> list, Comparator<? super T> comp) {
        if (list.size() <= 1) return;
        quickSortRec(list, 0, list.size() - 1, comp);
    }

    // ---- DELIVERY-SPECIFIC SORTING ----

public static void sortOrdersByDeliveryTimeSelection(
        DynamicArray<Order> orders) {

    if (orders == null) {
        return;
    }

    selectionSort(orders, (a, b) -> {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;

        return compareFiniteDouble(
                a.getEstimatedDeliveryTimeMin(),
                b.getEstimatedDeliveryTimeMin()
        );
    });
}

public static void sortOrdersByPriorityInsertion(
        DynamicArray<Order> orders) {

    if (orders == null) {
        return;
    }

    insertionSort(orders, (a, b) -> {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;

        double aPriority = a.getPriority();
        double bPriority = b.getPriority();

        boolean aValid =
                !Double.isNaN(aPriority)
                && !Double.isInfinite(aPriority);

        boolean bValid =
                !Double.isNaN(bPriority)
                && !Double.isInfinite(bPriority);

        if (!aValid && !bValid) {
            return Integer.compare(
                    a.getOrderId(),
                    b.getOrderId()
            );
        }

        if (!aValid) {
            return 1;
        }

        if (!bValid) {
            return -1;
        }

        int byPriority =
                Double.compare(bPriority, aPriority);

        if (byPriority != 0) {
            return byPriority;
        }

        return Integer.compare(
                a.getOrderId(),
                b.getOrderId()
        );
    });
}

public static void sortOrdersByRestaurantMerge(
        DynamicArray<Order> orders) {

    if (orders == null) {
        return;
    }

    mergeSort(orders, (a, b) -> {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;

        String left = a.getRestaurant();
        String right = b.getRestaurant();

        if (left == null && right == null) return 0;
        if (left == null) return 1;
        if (right == null) return -1;

        return left.compareToIgnoreCase(right);
    });
}

public static void sortOrdersByIdQuick(
        DynamicArray<Order> orders) {

    if (orders == null) {
        return;
    }

    quickSort(orders, (a, b) -> {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;

        return Integer.compare(
                a.getOrderId(),
                b.getOrderId()
        );
    });
}

public static void sortRidersByCompletedDeliveriesQuick(
        DynamicArray<Resource> riders) {

    if (riders == null) {
        return;
    }

    quickSort(riders, (a, b) -> {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;

        int byCompleted = Integer.compare(
                b.getCompletedDeliveries(),
                a.getCompletedDeliveries()
        );

        if (byCompleted != 0) {
            return byCompleted;
        }

        return Integer.compare(
                a.getResourceId(),
                b.getResourceId()
        );
    });
}

public static class RestaurantPopularity {
    public final String restaurant;
    public final int orderCount;

    public RestaurantPopularity(
            String restaurant,
            int orderCount) {

        this.restaurant = restaurant;
        this.orderCount = orderCount;
    }

    @Override
    public String toString() {
        return restaurant + " (" + orderCount + " orders)";
    }
}

public static DynamicArray<RestaurantPopularity>
        rankRestaurantsByPopularity(
                DynamicArray<Order> orders) {

    DynamicArray<RestaurantPopularity> result =
            new DynamicArray<>();

    if (orders == null || orders.isEmpty()) {
        return result;
    }

    HashTable<String, Integer> counts = new HashTable<>();
    HashTable<String, String> displayNames = new HashTable<>();

    for (Order order : orders) {
        if (order == null || order.getRestaurant() == null) {
            continue;
        }

        String restaurant = order.getRestaurant().trim();

        if (restaurant.isEmpty()) {
            continue;
        }

        String key = restaurant.toLowerCase();

        Integer currentCount = counts.get(key);

        if (currentCount == null) {
            counts.put(key, 1);
            displayNames.put(key, restaurant);
        } else {
            counts.put(key, currentCount + 1);
        }
    }

    for (HashTable.Entry<String, Integer> entry
            : counts.entries()) {

        String displayName = displayNames.get(entry.key);

        result.add(
                new RestaurantPopularity(
                        displayName,
                        entry.value
                )
        );
    }

    mergeSort(result, (a, b) -> {
        int byPopularity = Integer.compare(
                b.orderCount,
                a.orderCount
        );

        if (byPopularity != 0) {
            return byPopularity;
        }

        return a.restaurant.compareToIgnoreCase(
                b.restaurant
        );
    });

    return result;
}

private static int compareFiniteDouble(
        double a,
        double b) {

    boolean aValid =
            !Double.isNaN(a) && !Double.isInfinite(a);

    boolean bValid =
            !Double.isNaN(b) && !Double.isInfinite(b);

    if (!aValid && !bValid) {
        return 0;
    }

    if (!aValid) {
        return 1;
    }

    if (!bValid) {
        return -1;
    }

    return Double.compare(a, b);
}

    private static <T> void quickSortRec(DynamicArray<T> list, int low, int high, Comparator<? super T> comp) {
        if (low < high) {
            int pi = partition(list, low, high, comp);
            quickSortRec(list, low, pi - 1, comp);
            quickSortRec(list, pi + 1, high, comp);
        }
    }

    private static <T> int partition(DynamicArray<T> list, int low, int high, Comparator<? super T> comp) {
        T pivot = list.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (comp.compare(list.get(j), pivot) < 0) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, high);
        return i + 1;
    }

    private static <T> void swap(DynamicArray<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}

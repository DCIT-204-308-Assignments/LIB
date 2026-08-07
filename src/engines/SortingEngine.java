package engines;

import java.util.Comparator;
import ds.DynamicArray;

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

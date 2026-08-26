package com.group2.sorting;

class QuickSortTest extends SortAlgorithmContractTest {

    @Override
    protected Sorter<Integer> sorter() {
        return QuickSort::sort;
    }
}

package com.group2.sorting;

class InsertionSortTest extends SortAlgorithmContractTest {

    @Override
    protected Sorter<Integer> sorter() {
        return InsertionSort::sort;
    }
}

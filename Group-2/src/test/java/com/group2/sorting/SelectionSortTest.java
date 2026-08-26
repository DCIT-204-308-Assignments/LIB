package com.group2.sorting;

class SelectionSortTest extends SortAlgorithmContractTest {

    @Override
    protected Sorter<Integer> sorter() {
        return SelectionSort::sort;
    }
}

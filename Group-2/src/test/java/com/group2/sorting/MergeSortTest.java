package com.group2.sorting;

class MergeSortTest extends SortAlgorithmContractTest {

    @Override
    protected Sorter<Integer> sorter() {
        return MergeSort::sort;
    }
}

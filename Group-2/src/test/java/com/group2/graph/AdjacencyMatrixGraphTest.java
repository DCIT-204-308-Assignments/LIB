package com.group2.graph;

class AdjacencyMatrixGraphTest extends GraphContractTest {

    @Override
    protected Graph<String> createGraph(boolean directed) {
        return new AdjacencyMatrixGraph<>(directed);
    }
}

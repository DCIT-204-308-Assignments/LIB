package com.group2.graph;

class AdjacencyListGraphTest extends GraphContractTest {

    @Override
    protected Graph<String> createGraph(boolean directed) {
        return new AdjacencyListGraph<>(directed);
    }
}

package com.sorbonne;

import org.neo4j.procedure.*;
import org.neo4j.graphdb.Transaction;

import java.util.List;
import java.util.Map;

public class ConnectInputsRowToNeuron {

    @Context
    public Transaction tx;

    @Procedure(name = "nn.connectInputsRowToNeuron", mode = Mode.WRITE)
    @Description("Connecte chaque Row aux neurones d'entrées.")
    public void connectInputsRowToNeuron(
            @Name("network_structure") List<Long> networkStructure,
            @Name("batch_size") long batchSize) {

        int layerIndex = 0; // Couche d'entrée (index 0)
        int numNeurons = networkStructure.get(layerIndex).intValue(); 


        tx.execute("""
            UNWIND range(0, $batchSize - 1) AS rowIndex
            UNWIND range(0, $numNeurons - 1) AS neuronIndex
            MATCH (n1:Row {id: toString(rowIndex), type: 'inputsRow'})
            MATCH (n2:Neuron {id: toString($layerIndex) + '-' + toString(neuronIndex), type: 'input'})
            CREATE (n1)-[:CONTAINS {output: 0, id: toString(rowIndex) + "_" + toString(neuronIndex)}]->(n2)
        """, Map.of("batchSize", batchSize, "numNeurons", numNeurons, "layerIndex", layerIndex));
    }
}

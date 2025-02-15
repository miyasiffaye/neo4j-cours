package com.sorbonne;

import org.neo4j.procedure.*;
import org.neo4j.graphdb.Transaction;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class ConnectInputsRowToNeuron {

    @Context
    public Transaction tx;

    @Procedure(name = "nn.connectInputsRowToNeuron", mode = Mode.WRITE)
    @Description("Crée des nœuds Row et connecte chaque Row aux neurones d'entrée.")
    public void connectInputsRowToNeuron(
            @Name("network_structure") List<Long> networkStructure,
            @Name("batch_size") long batchSize) {

        int layerIndex = 0; // La couche d'entrée est toujours l'index 0
        int numNeurons = networkStructure.get(layerIndex).intValue(); // Nombre de neurones

        IntStream.range(0, (int) batchSize).forEach(rowIndex -> {
            tx.execute("CREATE (n:Row {id: $id, type: 'inputsRow'})",
                    Map.of("id", String.valueOf(rowIndex)));
        });

        IntStream.range(0, (int) batchSize).forEach(rowIndex -> {
            IntStream.range(0, numNeurons).forEach(neuronIndex -> {
                String neuronId = layerIndex + "-" + neuronIndex;
                String inputFeatureId = rowIndex + "_" + neuronIndex;

                tx.execute(
                        "MATCH (n1:Row {id: $fromId, type: 'inputsRow'}) " +
                                "MATCH (n2:Neuron {id: $toId, type: 'input'}) " +
                                "CREATE (n1)-[:CONTAINS {output: $value, id: $inputFeatureId}]->(n2)",
                        Map.of(
                                "fromId", String.valueOf(rowIndex),
                                "toId", neuronId,
                                "value", 0,
                                "inputFeatureId", inputFeatureId
                        )
                );
            });
        });
    }
}

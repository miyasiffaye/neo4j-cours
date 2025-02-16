package com.sorbonne;

import org.neo4j.procedure.*;

import java.util.Map;

public class ConnectOutputsRowToNeuron {

    @Context
    public org.neo4j.graphdb.Transaction tx;

    @Procedure(name = "nn.connectOutputsRowToNeuron", mode = Mode.WRITE)
    @Description("Connects an output from a row to a neuron.")
    public void connectOutputsRowToNeuron(
            @Name("layer_index") long layerIndex,  // Change int to long
            @Name("neuron_index") long neuronIndex,  // Change int to long
            @Name("row_index") long rowIndex) {  // Change int to long

        String fromId = layerIndex + "-" + neuronIndex;
        String toId = String.valueOf(rowIndex);
        String outputByRowId = rowIndex + "_" + neuronIndex;
        double value = 0.0; // Ou toute autre valeur que tu souhaites

        String query = "MATCH (n1:Neuron {id: $fromId, type:'output'}) " +
                "MATCH (n2:Row {id: $toId, type:'outputsRow'}) " +
                "CREATE (n1)-[:CONTAINS {output: $value, id: $outputByRowId}]->(n2)";

        tx.execute(query, Map.of(
                "fromId", fromId,
                "toId", toId,
                "outputByRowId", outputByRowId,
                "value", value
        ));
    }
}

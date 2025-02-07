package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SetExpectedOutputs {
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.setExpectedOutputs", mode = Mode.WRITE)
    @Description("Met à jour les sorties attendues des neurones de sortie.")
    public Stream<Result> setExpectedOutputs(@Name("dataset") List<Map<String, Object>> dataset,
                                             @Name("output_layer_index") long outputLayerIndex) {
        try (Transaction tx = db.beginTx()) {
            for (int rowIndex = 0; rowIndex < dataset.size(); rowIndex++) {
                Map<String, Object> row = dataset.get(rowIndex);
                Map<String, Object> expectedOutputs = (Map<String, Object>) row.get("expected_outputs");

                for (Map.Entry<String, Object> entry : expectedOutputs.entrySet()) {
                    String propertyName = "expected_output_" + rowIndex + "_0";
                    String query = "MATCH (:Neuron {type:'output', id: $outputneuronid})" +
                            "-[r:CONTAINS {id: $predictedoutputid}]->" +
                            "(row:Row {type:'outputsRow', id: $rowid}) " +
                            "SET r.expected_output = $value";

                    tx.execute(query, Map.of(
                            "rowid", rowIndex + "",
                            "predictedoutputid", rowIndex + "_0",
                            "outputneuronid", outputLayerIndex + "-0",
                            "value", entry.getValue()
                    ));
                }
            }
            tx.commit();
            return Stream.of(new Result("Expected outputs updated successfully!"));
        } catch (Exception e) {
            return Stream.of(new Result("Error: " + e.getMessage()));
        }
    }

    public static class Result {
        public String message;
        public Result(String message) { this.message = message; }
    }
}

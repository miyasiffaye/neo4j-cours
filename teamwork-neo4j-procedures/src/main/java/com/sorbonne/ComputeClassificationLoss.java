package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class ComputeClassificationLoss {
    @Context
    public Transaction tx;

    @Procedure(name = "nn.computeClassificationLoss",mode = Mode.READ)
    @Description("Compute classification loss over the neural network")
    public Stream<LossResult> computeClassificationLoss() {
        try {
            return tx.execute("""
                MATCH (output:Neuron {type: 'output'})
                MATCH (output)-[outputsValues_R:CONTAINS]->(row_for_outputs:Row {type: 'outputsRow'})
                WITH outputsValues_R,
                COALESCE(outputsValues_R.output, 0) AS predicted,
                COALESCE(outputsValues_R.expected_output, 0) AS actual,
                1e-10 AS epsilon
                RETURN SUM(
                 -actual * LOG(predicted + epsilon) - (1 - actual) * LOG(1 - predicted + epsilon)
                ) AS loss
            """).stream()
            .map(row -> new LossResult("Success", (Double) row.get("loss")));
        } catch (Exception e) {
            return Stream.of(new LossResult("Error: " + e.getMessage(), Double.NaN));
        }
    }

    public static class LossResult {
        public final String status;
        public final Double result;

        public LossResult(String status, Double result) {
            this.status = status;
            this.result = result;
        }
    }
}

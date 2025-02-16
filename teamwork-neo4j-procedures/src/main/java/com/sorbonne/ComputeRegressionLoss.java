package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

import java.util.stream.Stream;

public class ComputeRegressionLoss {
    @Context
    public Transaction tx;

    @Procedure(name = "nn.computeRegressionLoss",mode = Mode.READ)
    @Description("Compute regression loss over the neural network")
    public Stream<LossResult> computeRegressionLoss() {
        try {
            return tx.execute("""
                    MATCH (output:Neuron {type: 'output'})
                    MATCH (output)-[outputsValues_R:CONTAINS]->(row_for_outputs:Row {type: 'outputsRow'})
                    WITH outputsValues_R,
                    COALESCE(outputsValues_R.output, 0) AS predicted,
                    COALESCE(outputsValues_R.expected_output, 0) AS actual
                    RETURN AVG((predicted - actual)^2) AS loss
            """).stream()
            .map(row -> new LossResult("Success", (Double) row.get("loss")));
        } catch (Exception e) {
            return Stream.of(new LossResult("Error: " + e.getMessage(), Double.NaN));
        }
    }

    public static class LossResult {
        public final String status;
        public final Double loss;

        public LossResult(String status, Double loss) {
            this.status = status;
            this.loss = loss;
        }
    }
}

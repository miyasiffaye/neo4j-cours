package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

import java.util.stream.Stream;

public class ComputeRegressionLoss {
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.computeRegressionLoss",mode = Mode.READ)
    @Description("")
    public Stream<LossResult> computeRegressionLoss() {
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> nodes = tx.findNodes(Label.label("Neuron"), "type","output");
            Double loss = nodes.stream()
                .flatMap(n -> n.getRelationships(RelationshipType.withName("CONTAINS")).stream())
                .mapToDouble(r -> {
                    Object predictedObj = r.getProperty("output", 0.0);
                    Object actualObj = r.getProperty("expected_output", 0.0);

                    Double predicted = predictedObj instanceof Double ?
                            (Double) predictedObj : ((Long) predictedObj).doubleValue();
                    Double actual = actualObj instanceof Double ?
                            (Double) actualObj : ((Long) actualObj).doubleValue();

                    return Math.pow(predicted - actual, 2);
                }).average().orElse(Double.NaN);
            return Stream.of(new LossResult("Success", loss));
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

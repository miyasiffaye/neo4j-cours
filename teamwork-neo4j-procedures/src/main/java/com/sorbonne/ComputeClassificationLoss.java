package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class ComputeClassificationLoss {
    private static final double EPSILON = 1e-10;

    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.computeClassificationLoss",mode = Mode.READ)
    @Description("")
    public Stream<LossResult> computeClassificationLoss() {
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

                    return -actual * Math.log(predicted + EPSILON) - (1 - actual) * Math.log(1 - predicted + EPSILON);
                }).sum();
            return Stream.of(new LossResult("Success", loss));
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

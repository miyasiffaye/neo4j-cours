package com.sorbonne;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class EvaluateModel {
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.evaluateModel", mode = Mode.READ)
    @Description("Returns a map of output neuron IDs and their predicted values.")
    public Stream<OutputRecord> evaluateModel() {
        try (Transaction tx = db.beginTx()) {
            Result result = tx.execute("""
                        MATCH (n:Neuron {type: 'output'})
                        RETURN n.id AS id, n.output AS predicted
                    """);

            List<OutputRecord> output = new ArrayList<>();
            while(result.hasNext()) {
                Map<String, Object> record = result.next();
                String id = (String) record.get("id");
                Double predicted = (Double) record.get("predicted");
                output.add(new OutputRecord(id, predicted));
            }
            return output.stream();
        }
    }

    public static class OutputRecord {
        public final String id;
        public final Double predicted;

        public OutputRecord(String id, Double predicted) {
            this.id = id;
            this.predicted = predicted;
        }
    }
}
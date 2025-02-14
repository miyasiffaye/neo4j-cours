package com.sorbonne;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ExpectedOutput {
    @Context
    public Log log;
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.expectedOutput", mode = Mode.READ)
    @Description("Retrieve the expected outputs of the neurons")
    public Stream<ExpectedOutputResult> expectedOutput() {
        try (Transaction tx = db.beginTx()) {
            Result result = tx
                    .execute("MATCH (n:Neuron {type: 'output'}) RETURN n.id AS id, n.expected_output AS expected");

            Map<String, Double> expectedOutputs = new HashMap<>();
            while (result.hasNext()) {
                Map<String, Object> record = result.next();
                expectedOutputs.put((String) record.get("id"), (Double) record.get("expected"));
            }

            return expectedOutputs.entrySet().stream()
                    .map(entry -> new ExpectedOutputResult(entry.getKey(), entry.getValue()));

        }

    }


    public static class ExpectedOutputResult {
        public String id;
        public double expected;

        public ExpectedOutputResult(String id, double expected) {
            this.id = id;
            this.expected = expected;
        }
    }
}

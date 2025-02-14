package com.sorbonne;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

import java.util.stream.Stream;

public class ConstrainWeights {

    @Context
    public Log log;
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.constrainWeights", mode = Mode.READ)
    @Description("Constrain the weights of relationships to be within the range [-1.0, 1.0]")
    public Stream<CreateResult> constrainWeights() {
        String query = "MATCH ()-[r:CONNECTED_TO]->() " +
                "SET r.weight = CASE " +
                "WHEN r.weight > 1.0 THEN 1.0 " +
                "WHEN r.weight < -1.0 THEN -1.0 " +
                "ELSE r.weight " +
                "END";
        try (Transaction tx = db.beginTx()) {
            tx.execute(query);
            tx.commit();
            return Stream.of(new CreateResult("ok"));
        }
    }

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

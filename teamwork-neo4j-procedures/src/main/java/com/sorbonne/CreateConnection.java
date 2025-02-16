package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateConnection {
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.createConnection",mode = Mode.WRITE)
    @Description("Creates a connection between two neurones.")
    public Stream<CreateResult> createConnection(@Name("from_id") String fromId,
                                                 @Name("to_id") String toId,
                                                 @Name("weight") Double weight) {
        try (Transaction tx = db.beginTx()) {
            tx.execute("MATCH (n1:Neuron {id: '" + fromId + "' })\n" +
                          "MATCH (n2:Neuron {id: '" + toId + "'})\n" +
                          "CREATE (n1)-[:CONNECTED_TO {weight: " + weight + "}] -> (n2)");
            tx.commit();
            return Stream.of(new CreateResult("Success"));
        } catch (Exception e) {
            return Stream.of(new CreateResult("Error: " + e.getMessage()));
        }
    }

    public static class CreateResult {
        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}


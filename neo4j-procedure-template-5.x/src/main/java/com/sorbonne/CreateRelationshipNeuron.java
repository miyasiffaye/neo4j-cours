package com.sorbonne;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateRelationshipNeuron {

    @Context
    public Log log;
    @Context
    public GraphDatabaseService db;
    @Procedure(name = "nn.createRelationshipsNeuron", mode = Mode.WRITE)
    @Description("")

    public Stream<CreateResult> createRelationShipsNeuron(
            @Name("from_id") String from_id,
            @Name("to_id") String to_id,
            @Name("weight") String weight) {
        try (Transaction tx = db.beginTx()) {

            tx.execute(
                    "MATCH (n1:Neuron" + "{id:'" + from_id + "'})\n" +
                            "MATCH (n2:Neuron" + "{id:'" + to_id + "'})\n" +
                            "CREATE (n1)-[:CONNECTED_TO {weight:" + weight + "}]->(n2)");
            tx.commit();
            return Stream.of(new CreateResult("ok"));

        } catch (Exception e) {

            return Stream.of(new CreateResult("ko"));
        }
    }


    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateConnection {
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db; //référence a la base de données neo4J pour éxécuter les requêtes et manipuler des graphes

    @Procedure(name = "nn.createConnection",mode = Mode.WRITE)
    //mode WRITE car va modifier la base de données
    @Description("Creates the connection between two neurons with a weight")
    public Stream<CreateConnection.CreateResult> createConnection(@Name("from_id") String from_id,
                                                          @Name("to_id") String to_id,
                                                          @Name("weight") double weight

    ) {

        /*Version proposé par l'enseignant*/
        try (Transaction tx = db.beginTx()) {

            tx.execute("MATCH (n1:Neuron {id: $from_id})\n" +
                    "MATCH (n2:Neuron {id: $to_id})\n" +
                    "CREATE (n1)-[:CONNECTED_TO {weight: $weight}]->(n2)");
            return Stream.of(new CreateConnection.CreateResult("ok"));

        } catch (Exception e) {

            return Stream.of(new CreateConnection.CreateResult("ko"));
        }
    }

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }


}

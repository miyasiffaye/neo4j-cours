package com.sorbonne;

import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

public class ConnectNeuron {

    // Injection du service de base de données Neo4j
    @Context
    public GraphDatabaseService db;

    // Classe pour retourner un message de confirmation (facultatif)
    public static class MessageResult {
        public String message;

        public MessageResult(String message) {
            this.message = message;
        }
    }


    @Procedure(name = "nn.connectNeurons", mode = Mode.WRITE)
    @Description("Crée une relation CONNECTED_TO entre deux neurones identifiés par 'from_id' et 'to_id' avec le poids 'weight'.")
    public Stream<MessageResult> connectNeurons(
            @Name("from_id") String fromId,
            @Name("to_id") String toId,
            @Name("weight") double weight
    ) {

        try (Transaction tx = db.beginTx()) {

            tx.execute("MATCH (n1:Neuron {id: '" + fromId + "'}) \n" +
                    "MATCH (n2:Neuron {id: '" + toId + "'}) \n" +
                    "CREATE (n1)-[:CONNECTED_TO {weight: '" + weight + "'}]->(n2)");
            tx.commit();
            return Stream.of(new MessageResult("OK"));
        } catch (Exception e) {
            return Stream.of(new MessageResult("KO"));
        }
    }
}

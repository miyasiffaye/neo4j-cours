package com.sorbonne;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;
import java.util.Map;
import java.util.stream.Stream;

public class CreateRowNode {

    // Injection du service de base de données Neo4j
    @Context
    public GraphDatabaseService db;
    //public Transaction tx;

    // Classe pour retourner un message de confirmation (facultatif)
    public static class MessageResult {
        public String message;

        public MessageResult(String message) {
            this.message = message;
        }
    }


    @Procedure(name = "nn.createRowNode", mode = Mode.WRITE)
    @Description("Crée un nœud Row avec l'id fourni et le type 'inputsRow'.")
    public Stream<MessageResult> createInputRow(@Name("id") String id) {
        String query = "CREATE (n:Row { id: '" + id + "', type: 'inputsRow' })";
        try (Transaction tx = db.beginTx()) {
            tx.execute(query, Map.of("id", id));
            tx.commit();
            return Stream.of(new MessageResult("Nœud Row créé avec l'id: " + id));
        }
    }
}
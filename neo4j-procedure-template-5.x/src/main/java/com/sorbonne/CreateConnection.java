package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateConnection {
    @Context
    public Log log;  // Pour enregistrer les messages dans les logs
    @Context
    public GraphDatabaseService db;  // Référence à la base de données Neo4j pour exécuter des requêtes

    @Procedure(name = "nn.createConnection", mode = Mode.WRITE)
    @Description("Creates the connection between two neurons with a weight")
    public Stream<CreateConnection.CreateResult> createConnection(
            @Name("from_id") String from_id,
            @Name("to_id") String to_id,
            @Name("weight") double weight
    ) {
        try (Transaction tx = db.beginTx()) {
            // Requête Cypher avec des concaténations de chaînes
            String cypherQuery = "MATCH (n1:Neuron {id: '" + from_id + "'})\n" +
                    "MATCH (n2:Neuron {id: '" + to_id + "'})\n" +
                    "CREATE (n1)-[:CONNECTED_TO {weight: " + weight + "}]->(n2)";

            // Exécuter la requête Cypher
            tx.execute(cypherQuery);
            tx.commit();  // Assurez-vous que la transaction est bien engagée

            // Si tout se passe bien, retour "ok"
            return Stream.of(new CreateConnection.CreateResult("ok"));

        } catch (Exception e) {
            // Enregistrer l'erreur dans les logs Neo4j
            log.error("Error in createConnection procedure: " + e.getMessage(), e);
            // Retourner "ko" en cas d'erreur
            return Stream.of(new CreateConnection.CreateResult("ko"));
        }
    }

    // Classe interne pour retourner le résultat
    public static class CreateResult {
        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

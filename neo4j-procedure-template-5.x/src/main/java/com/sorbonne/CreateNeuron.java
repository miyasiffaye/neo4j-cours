package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateNeuron {
    @Context
    public Log log;  // Pour enregistrer les messages dans les logs
    @Context
    public GraphDatabaseService db;  // Référence à la base de données Neo4j pour exécuter des requêtes

    @Procedure(name = "nn.createNeuron", mode = Mode.WRITE)
    @Description("Creates a neuron")
    public Stream<CreateResult> createNeuron(
            @Name("id") String id,
            @Name("layer") long layer,
            @Name("type") String type,
            @Name("activation_function") String activation_function
    ) {
        try (Transaction tx = db.beginTx()) {

            // Créer la requête Cypher avec des paramètres directement insérés dans la requête
            String cypherQuery = "CREATE (n:Neuron {id: '" + id + "', layer: " + layer + ", type: '" + type + "', bias: 0.0, output: null, m_bias: 0.0, v_bias: 0.0, activation_function: '" + activation_function + "'})";

            // Exécuter la requête Cypher
            tx.execute(cypherQuery);
            tx.commit();  // Assurez-vous que la transaction est bien engagée

            // Si tout se passe bien, retour "ok"
            return Stream.of(new CreateResult("ok"));

        } catch (Exception e) {
            // Enregistrer l'erreur dans les logs Neo4j
            log.error("Error in createNeuron procedure: " + e.getMessage(), e);
            // Retourner "ko" en cas d'erreur
            return Stream.of(new CreateResult("ko"));
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

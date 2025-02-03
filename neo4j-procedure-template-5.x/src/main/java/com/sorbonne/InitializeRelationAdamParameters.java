package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class InitializeRelationAdamParameters {
    @Context
    public Log log; // Permet de loguer des messages
    @Context
    public GraphDatabaseService db; // Référence à la base de données Neo4j

    @Procedure(name = "nn.initializeRelationAdamParameters", mode = Mode.WRITE)
    @Description("Initialize Adam parameters in the relations (connections)")
    public Stream<InitializeRelationAdamParameters.CreateResult> initializeRelationAdamParameters() {

        try (Transaction tx = db.beginTx()) {
            // Initialisation des paramètres Adam pour chaque relation CONNECTED_TO
            tx.execute("MATCH ()-[r:CONNECTED_TO]->() SET r.m = 0.0, r.v = 0.0");

            // Confirmer que l'initialisation s'est bien passée
            log.info("Adam parameters initialized in all relations");

            tx.commit(); // Confirmer la transaction
            return Stream.of(new CreateResult("ok", "Adam parameters successfully initialized"));

        } catch (Exception e) {
            // Loguer l'exception en cas d'erreur
            log.error("Error initializing Adam parameters in relations: " + e.getMessage(), e);
            return Stream.of(new CreateResult("ko", "Error initializing Adam parameters"));
        }
    }

    public static class CreateResult {
        public final String result; // Résultat de l'opération (ok/ko)
        public final String message; // Message détaillant le résultat

        public CreateResult(String result, String message) {
            this.result = result;
            this.message = message;
        }
    }
}

package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class ConstrainWeights {
    @Context
    public Log log; // Permet de loguer des messages
    @Context
    public GraphDatabaseService db; // Référence à la base de données Neo4j

    @Procedure(name = "nn.constrainWeights", mode = Mode.WRITE)
    @Description("Constrains the weights to be between -1 and 1")
    public Stream<ConstrainWeights.CreateResult> constrainWeights() {

        try (Transaction tx = db.beginTx()) {
            // Log avant l'exécution de la requête pour avoir une trace
            log.info("Constraining weights of all connections between neurons");

            // Requête qui contraint les poids à être entre -1 et 1
            tx.execute("MATCH ()-[r:CONNECTED_TO]->() " +
                    "SET r.weight = CASE " +
                    "    WHEN r.weight > 1.0 THEN 1.0 " +
                    "    WHEN r.weight < -1.0 THEN -1.0 " +
                    "    ELSE r.weight " +
                    "END");

            // Log après l'exécution de la requête pour confirmer l'action
            log.info("Weights constrained to the range [-1, 1]");

            // Commit de la transaction
            tx.commit();

            return Stream.of(new CreateResult("ok", "Weights constrained successfully"));
        } catch (Exception e) {
            // Log de l'exception pour le débogage
            log.error("Error constraining weights: " + e.getMessage(), e);

            return Stream.of(new CreateResult("ko", "Error constraining weights"));
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

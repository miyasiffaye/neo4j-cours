package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;
import java.util.HashMap;
import java.util.Map;

public class CreateLossRegression {
    @Context
    public Log log; // Permet de loguer des messages
    @Context
    public GraphDatabaseService db; // Référence à la base de données Neo4j

    @Procedure(name = "nn.createLossRegression", mode = Mode.WRITE)
    @Description("Creates loss for regression model")
    public Stream<CreateLossRegression.CreateResult> createLossRegression(
            @Name("epsilon") double epsilon // Paramètre epsilon (optionnel)
    ) {
        try (Transaction tx = db.beginTx()) {

            // Requête Cypher pour calculer la perte
            String cypherQuery = "MATCH (output:Neuron {type: 'output'})\n" +
                    "                    MATCH (output)-[outputsValues_R:CONTAINS]->(row_for_outputs:Row {type: 'outputsRow'})\n" +
                    "                    WITH outputsValues_R,\n" +
                    "                         COALESCE(outputsValues_R.output, 0) AS predicted,\n" +
                    "                         COALESCE(outputsValues_R.expected_output, 0) AS actual,\n" +
                    "                         $epsilon AS epsilon\n" +
                    "                    RETURN AVG( (predicted - actual)^2 ) AS loss";

            // Paramétrage de la requête avec l'epsilon
            Map<String, Object> params = new HashMap<>();
            params.put("epsilon", epsilon);

            // Exécution de la requête Cypher avec les paramètres
            Result result = tx.execute(cypherQuery, params);

            // Récupérer la perte calculée
            double loss = (double) result.next().get("loss");

            // Loguer la perte
            log.info("Calculated loss for regression: " + loss);

            tx.commit(); // Valider la transaction

            return Stream.of(new CreateLossRegression.CreateResult("ok", loss));

        } catch (Exception e) {
            // Loguer l'exception pour faciliter le diagnostic
            log.error("Error in createLossRegression procedure: " + e.getMessage(), e);
            return Stream.of(new CreateLossRegression.CreateResult("ko", -1.0)); // Retourner une valeur d'erreur (-1)
        }
    }

    public static class CreateResult {
        public final String result;
        public final double loss; // Perte calculée

        public CreateResult(String result, double loss) {
            this.result = result;
            this.loss = loss;
        }
    }
}

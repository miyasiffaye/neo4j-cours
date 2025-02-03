package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;
import java.util.HashMap;
import java.util.Map;

public class CreateLossClassification {
    @Context
    public Log log; // Permet de loguer des messages
    @Context
    public GraphDatabaseService db; // Référence à la base de données Neo4j

    @Procedure(name = "nn.createLossClassification", mode = Mode.WRITE)
    @Description("Creates loss for classification model")
    public Stream<CreateLossClassification.CreateResult> createLossClassification(
            @Name("epsilon") double epsilon // Paramètre epsilon
    ) {
        try (Transaction tx = db.beginTx()) {

            // Requête Cypher pour calculer la perte
            String cypherQuery = "MATCH (output:Neuron {type: 'output'})\n" +
                    "                    MATCH (output)-[outputsValues_R:CONTAINS]->(row_for_outputs:Row {type: 'outputsRow'})\n" +
                    "                    WITH outputsValues_R,\n" +
                    "                         COALESCE(outputsValues_R.output, 0) AS predicted,\n" +
                    "                         COALESCE(outputsValues_R.expected_output, 0) AS actual,\n" +
                    "                         $epsilon AS epsilon\n" +
                    "                    RETURN SUM(\n" +
                    "                        -actual * LOG(predicted + epsilon) - (1 - actual) * LOG(1 - predicted + epsilon)\n" +
                    "                    ) AS loss";

            // Passer le paramètre epsilon à la requête Cypher
            Map<String, Object> params = new HashMap<>();
            params.put("epsilon", epsilon);

            // Exécution de la requête Cypher avec les paramètres
            Result result = tx.execute(cypherQuery, params);

            // Si nécessaire, récupérer le résultat (par exemple, la valeur de la perte)
            double loss = (double) result.next().get("loss");

            // Loguer la perte calculée
            log.info("Calculated loss: " + loss);

            tx.commit(); // Valider la transaction

            return Stream.of(new CreateLossClassification.CreateResult("ok"));

        } catch (Exception e) {
            // Loguer l'exception pour mieux diagnostiquer les erreurs
            log.error("Error in createLossClassification procedure: " + e.getMessage(), e);
            return Stream.of(new CreateLossClassification.CreateResult("ko"));
        }
    }

    public static class CreateResult {
        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

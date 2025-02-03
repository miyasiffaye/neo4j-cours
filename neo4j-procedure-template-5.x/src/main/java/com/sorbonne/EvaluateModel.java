package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class EvaluateModel {
    @Context
    public Log log; // Permet de loguer des messages
    @Context
    public GraphDatabaseService db; // Référence à la base de données Neo4j

    @Procedure(name = "nn.evaluateModel", mode = Mode.WRITE)
    @Description("Evaluates the model by calculating the loss and metrics")
    public Stream<EvaluateModel.CreateResult> evaluateModel() {

        try (Transaction tx = db.beginTx()) {

            log.info("Evaluating model...");

            // Récupérer les neurones de sortie et leurs valeurs de sortie et attendues
            Result result = tx.execute("MATCH (n:Neuron {type: 'output'}) " +
                    "MATCH (n)-[r:CONTAINS]->(row:Row {type: 'outputsRow'}) " +
                    "RETURN n.id AS id, n.output AS predicted, row.expected_output AS expected");

            double totalLoss = 0.0;
            int count = 0;

            // Calcul de la perte (loss) en utilisant l'erreur quadratique moyenne (MSE) pour la régression
            // ou une fonction de perte de classification (comme l'entropie croisée) selon le type de modèle
            while (result.hasNext()) {
                // Accéder directement aux résultats sans utiliser Map
                Object predictedObj = result.next().get("predicted");
                Object expectedObj = result.next().get("expected_output");

                // Convertir les résultats en double
                double predicted = (predictedObj instanceof Double) ? (Double) predictedObj : 0.0;
                double expected = (expectedObj instanceof Double) ? (Double) expectedObj : 0.0;

                // Calcul de l'erreur quadratique pour chaque sortie
                totalLoss += Math.pow(predicted - expected, 2);
                count++;
            }

            // Calcul de la moyenne de la perte (MSE)
            double averageLoss = totalLoss / count;
            log.info("Average Loss: " + averageLoss);

            // Retourner les résultats de l'évaluation
            return Stream.of(new CreateResult("ok", "Model evaluated successfully with average loss: " + averageLoss));

        } catch (Exception e) {
            // Log l'erreur
            log.error("Error evaluating the model: " + e.getMessage(), e);
            return Stream.of(new CreateResult("ko", "Error evaluating model"));
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

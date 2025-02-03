package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateExpectedOutput {
    @Context
    public Log log; // Permet de loguer des messages
    @Context
    public GraphDatabaseService db; // Référence à la base de données Neo4j

    @Procedure(name = "nn.createExpectedOutput", mode = Mode.WRITE)
    @Description("Gives the expected output")
    public Stream<CreateExpectedOutput.CreateResult> createExpectedOutput() {

        try (Transaction tx = db.beginTx()) {

            // Récupérer les neurones de sortie et leurs valeurs attendues
            Result result = tx.execute("MATCH (n:Neuron {type: 'output'}) " +
                    "RETURN n.id AS id, n.expected_output AS expected");

            while (result.hasNext()) {
                // Accéder directement aux résultats sans utiliser Map
                Object expectedOutputObj = result.next().get("expected");

                // Si la valeur est présente et est de type Double
                double expectedOutput = (expectedOutputObj instanceof Double) ? (Double) expectedOutputObj : 0.0;

                // Effectuer des opérations avec expectedOutput si nécessaire
                log.info("Neuron expected output: " + expectedOutput);
            }

            return Stream.of(new CreateExpectedOutput.CreateResult("ok"));

        } catch (Exception e) {
            // En cas d'exception, loguer l'erreur et retourner "ko"
            log.error("Error creating expected output: " + e.getMessage(), e);
            return Stream.of(new CreateExpectedOutput.CreateResult("ko"));
        }
    }

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

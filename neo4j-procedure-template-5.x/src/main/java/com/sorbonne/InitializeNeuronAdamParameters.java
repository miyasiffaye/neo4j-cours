package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class InitializeNeuronAdamParameters {
    @Context
    public Log log; // Permet de loguer des messages
    @Context
    public GraphDatabaseService db; // Référence à la base de données Neo4j

    @Procedure(name = "nn.initializeNeuronAdamParameters", mode = Mode.WRITE)
    @Description("Initialize Adam parameters in the neurons")
    public Stream<InitializeNeuronAdamParameters.CreateResult> initializeNeuronAdamParameters() {

        try (Transaction tx = db.beginTx()) {
            // Initialisation des paramètres Adam pour chaque neurone
            tx.execute("MATCH (n:Neuron) SET n.m_bias = 0.0, n.v_bias = 0.0");

            // Loguer l'opération
            log.info("Adam parameters initialized in all neurons");

            tx.commit(); // Commit de la transaction
            return Stream.of(new CreateResult("ok", "Adam parameters successfully initialized in neurons"));

        } catch (Exception e) {
            // Loguer l'exception en cas d'erreur
            log.error("Error initializing Adam parameters in neurons: " + e.getMessage(), e);
            return Stream.of(new CreateResult("ko", "Error initializing Adam parameters in neurons"));
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

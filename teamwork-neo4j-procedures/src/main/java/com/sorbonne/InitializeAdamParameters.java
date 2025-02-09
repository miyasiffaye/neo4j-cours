package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class InitializeAdamParameters {

    @Context
    public GraphDatabaseService db;

    // Procédure pour initialiser les paramètres Adam des connexions
    @Procedure(name = "nn.initialize_adam_connections", mode = Mode.WRITE)
    @Description("Initialise les paramètres Adam pour toutes les connexions entre neurones.")
    public Stream<Result> initializeAdamConnections() {
        try (Transaction tx = db.beginTx()) {
            tx.execute("MATCH ()-[r:CONNECTED_TO]->() SET r.m = 0.0, r.v = 0.0");
            tx.commit();
            return Stream.of(new Result("Adam parameters initialized for connections!"));
        } catch (Exception e) {
            return Stream.of(new Result("Error: " + e.getMessage()));
        }
    }

    // Procédure pour initialiser les paramètres Adam des neurones
    @Procedure(name = "nn.initialize_adam_neurons", mode = Mode.WRITE)
    @Description("Initialise les paramètres Adam pour tous les neurones.")
    public Stream<Result> initializeAdamNeurons() {
        try (Transaction tx = db.beginTx()) {
            tx.execute("MATCH (n:Neuron) SET n.m_bias = 0.0, n.v_bias = 0.0");
            tx.commit();
            return Stream.of(new Result("Adam parameters initialized for neurons!"));
        } catch (Exception e) {
            return Stream.of(new Result("Error: " + e.getMessage()));
        }
    }

    public static class Result {
        public String message;
        public Result(String message) { this.message = message; }
    }
}

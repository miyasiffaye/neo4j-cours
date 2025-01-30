package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class InitializeNeuronAdamParameters {
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.initializeNeuronAdamParameters",mode = Mode.WRITE)
    //mode WRITE car va modifier la base de données
    @Description("Initialize Adam parameters in the neurons")
    public Stream<InitializeNeuronAdamParameters.CreateResult> initializeNeuronAdamParameters(
    ) {

        /*Version proposé par l'enseignant*/
        try (Transaction tx = db.beginTx()) {

            tx.execute("  MATCH (n:Neuron)\n" +
                    "                SET n.m_bias = 0.0, n.v_bias = 0.0");
            return Stream.of(new InitializeNeuronAdamParameters.CreateResult("ok"));

        } catch (Exception e) {

            return Stream.of(new InitializeNeuronAdamParameters.CreateResult("ko"));
        }
    }

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

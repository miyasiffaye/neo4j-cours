package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateExpectedOutput {
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.createExpectedOutput",mode = Mode.WRITE)
    //mode WRITE car va modifier la base de données
    @Description("Gives the expected output")
    public Stream<CreateExpectedOutput.CreateResult> createExpectedOutput(
    ) {

        /*Version proposé par l'enseignant*/
        try (Transaction tx = db.beginTx()) {

            tx.execute("MATCH (n:Neuron {type: 'output'})\n" +
                    "                RETURN n.id AS id, n.expected_output AS expected");
            return Stream.of(new CreateExpectedOutput.CreateResult("ok"));

        } catch (Exception e) {

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

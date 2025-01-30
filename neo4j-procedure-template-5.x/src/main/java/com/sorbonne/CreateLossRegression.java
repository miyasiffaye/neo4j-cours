package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateLossRegression {
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.createLossRegression",mode = Mode.WRITE)
    //mode WRITE car va modifier la base de données
    @Description("Creates loss for regression model")
    public Stream<CreateLossRegression.CreateResult> createLossRegression(
    ) {

        /*Version proposé par l'enseignant*/
        try (Transaction tx = db.beginTx()) {

            tx.execute(" MATCH (output:Neuron {type: 'output'})\n" +
                    "                    MATCH (output)-[outputsValues_R:CONTAINS]->(row_for_outputs:Row {type: 'outputsRow'})\n" +
                    "                    WITH outputsValues_R,\n" +
                    "                         COALESCE(outputsValues_R.output, 0) AS predicted,\n" +
                    "                         COALESCE(outputsValues_R.expected_output, 0) AS actual\n" +
                    "                    RETURN AVG((predicted - actual)^2) AS loss");
            return Stream.of(new CreateLossRegression.CreateResult("ok"));

        } catch (Exception e) {

            return Stream.of(new CreateLossRegression.CreateResult("ko"));
        }
    }

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

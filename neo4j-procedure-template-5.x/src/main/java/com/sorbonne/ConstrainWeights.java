package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class ConstrainWeights {
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.constrainWeights",mode = Mode.WRITE)
    //mode WRITE car va modifier la base de données
    @Description("Constrains the weights")
    public Stream<ConstrainWeights.CreateResult> constrainWeights(
    ) {

        /*Version proposé par l'enseignant*/
        try (Transaction tx = db.beginTx()) {

            tx.execute("MATCH ()-[r:CONNECTED_TO]->()\n" +
                    "                SET r.weight = CASE \n" +
                    "                    WHEN r.weight > 1.0 THEN 1.0 \n" +
                    "                    WHEN r.weight < -1.0 THEN -1.0 \n" +
                    "                    ELSE r.weight \n" +
                    "                END");
            return Stream.of(new ConstrainWeights.CreateResult("ok"));

        } catch (Exception e) {

            return Stream.of(new ConstrainWeights.CreateResult("ko"));
        }
    }

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class InitializeRelationAdamParameters {
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.initializeRelationAdamParameters",mode = Mode.WRITE)
    //mode WRITE car va modifier la base de données
    @Description("Initialize Adam parameters in the relations (connections)")
    public Stream<InitializeRelationAdamParameters.CreateResult> initializeRelationAdamParameters(
    ) {

        /*Version proposé par l'enseignant*/
        try (Transaction tx = db.beginTx()) {

            tx.execute("  MATCH ()-[r:CONNECTED_TO]->()\n" +
                    "                SET r.m = 0.0, r.v = 0.0");
            return Stream.of(new InitializeRelationAdamParameters.CreateResult("ok"));

        } catch (Exception e) {

            return Stream.of(new InitializeRelationAdamParameters.CreateResult("ko"));
        }
    }

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

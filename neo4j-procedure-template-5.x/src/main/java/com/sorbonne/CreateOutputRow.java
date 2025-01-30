package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateOutputRow {
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.createOutputRow",mode = Mode.WRITE)
    //mode WRITE car va modifier la base de données
    @Description("Creates a node Row of output type for each batch entry")
    public Stream<CreateOutputRow.CreateResult> createOutputRow(@Name("id") String id
    ) {

        /*Version proposé par l'enseignant*/
        try (Transaction tx = db.beginTx()) {

            tx.execute("CREATE (n:Row {\n" +
                    "                       id: $id,\n" +
                    "                       type: 'outputsRow'})");
            return Stream.of(new CreateOutputRow.CreateResult("ok"));

        } catch (Exception e) {

            return Stream.of(new CreateOutputRow.CreateResult("ko"));
        }
    }

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

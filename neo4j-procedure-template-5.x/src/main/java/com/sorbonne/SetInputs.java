package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class SetInputs {
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.setInputs",mode = Mode.WRITE)
    //mode WRITE car va modifier la base de données
    @Description("Sets the inputs")
    public Stream<SetInputs.CreateResult> setInputs(@Name("rowid") String rowid,
                                                    @Name("inputfeatureid") String inputfeatureid,
                                                    @Name("inputneuronid") String inputneuronid,
                                                    @Name("value") int value
    ) {

        /*Version proposé par l'enseignant*/
        try (Transaction tx = db.beginTx()) {

            tx.execute("MATCH (row:Row {{type:'inputsRow', id: $rowid}})-[r:CONTAINS {{ id: $inputfeatureid}}]->(inputs:Neuron {{type:'input', id: $inputneuronid}})\n" +
                    "                SET r.output = $value");
            return Stream.of(new SetInputs.CreateResult("ok"));

        } catch (Exception e) {

            return Stream.of(new SetInputs.CreateResult("ko"));
        }
    }

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

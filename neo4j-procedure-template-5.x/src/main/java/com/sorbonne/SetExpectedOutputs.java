package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;
public class SetExpectedOutputs {
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.setExpectedOutputs",mode = Mode.WRITE)
    //mode WRITE car va modifier la base de données
    @Description("Sets the expected outputs")
    public Stream<SetExpectedOutputs.CreateResult> setExpectedOutputs(@Name("rowid") String rowid,
                                                    @Name("predictedoutputid") String predictedoutputid,
                                                    @Name("outputneuronid") String outputneuronid,
                                                    @Name("value") int value
    ) {

        /*Version proposé par l'enseignant*/
        try (Transaction tx = db.beginTx()) {

            tx.execute("MATCH(:Neuron {{type:'output', id: $outputneuronid}})-[r:CONTAINS {{ id: $predictedoutputid}}]->(row:Row {{type:'outputsRow', id: $rowid}})\n" +
                    "                               SET r.expected_output = $value");
            return Stream.of(new SetExpectedOutputs.CreateResult("ok"));

        } catch (Exception e) {

            return Stream.of(new SetExpectedOutputs.CreateResult("ko"));
        }
    }

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

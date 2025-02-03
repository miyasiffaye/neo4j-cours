package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class SetExpectedOutputs {
    @Context
    public Log log; // Permet de loguer des messages
    @Context
    public GraphDatabaseService db; // Référence à la base de données Neo4j

    @Procedure(name = "nn.setExpectedOutputs", mode = Mode.WRITE)
    @Description("Sets the expected outputs")
    public Stream<SetExpectedOutputs.CreateResult> setExpectedOutputs(@Name("rowid") String rowid,
                                                                      @Name("predictedoutputid") String predictedoutputid,
                                                                      @Name("outputneuronid") String outputneuronid,
                                                                      @Name("value") long value) {

        /* Version proposée par l'enseignant */
        try (Transaction tx = db.beginTx()) {

            // Exécution de la requête Cypher sans utiliser Parameters
            String query = "MATCH(:Neuron {type: 'output', id: '" + outputneuronid + "'})-[:CONTAINS {id: '" + predictedoutputid + "'}]->" +
                    "(row:Row {type: 'outputsRow', id: '" + rowid + "'}) " +
                    "SET r.expected_output = " + value;

            // Exécution de la requête avec la chaîne formée
            tx.execute(query);

            return Stream.of(new SetExpectedOutputs.CreateResult("ok"));

        } catch (Exception e) {
            log.error("Error in setting expected outputs: " + e.getMessage(), e);
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

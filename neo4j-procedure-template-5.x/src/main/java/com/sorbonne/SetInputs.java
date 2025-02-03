package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class SetInputs {
    @Context
    public Log log; // Permet de loguer des messages
    @Context
    public GraphDatabaseService db; // Référence à la base de données Neo4j

    @Procedure(name = "nn.setInputs", mode = Mode.WRITE)
    @Description("Sets the inputs")
    public Stream<SetInputs.CreateResult> setInputs(@Name("rowid") String rowid,
                                                    @Name("inputfeatureid") String inputfeatureid,
                                                    @Name("inputneuronid") String inputneuronid,
                                                    @Name("value") long value) {

        /* Version proposée par l'enseignant */
        try (Transaction tx = db.beginTx()) {

            // Exécution de la requête Cypher sans utiliser Parameters
            String query = "MATCH (row:Row {type: 'inputsRow', id: '" + rowid + "'})-[:CONTAINS {id: '" + inputfeatureid + "'}]->(inputs:Neuron {type: 'input', id: '" + inputneuronid + "'}) " +
                    "SET r.output = " + value;

            // Exécution de la requête avec la chaîne formée
            tx.execute(query);

            return Stream.of(new SetInputs.CreateResult("ok"));

        } catch (Exception e) {
            log.error("Error in setting inputs: " + e.getMessage(), e);
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

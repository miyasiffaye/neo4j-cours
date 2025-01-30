package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateConnOutputNeuronOutputRow {
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db; //référence a la base de données neo4J pour éxécuter les requêtes et manipuler des graphes


    // Pour utiliser call nn.createNeuron("123","0","input","sotfmax")
    @Procedure(name = "nn.createConnOutputNeuronOutputRow",mode = Mode.WRITE)
    //mode WRITE car va modifier la base de données
    @Description("Creates connection between inputs neurons and inputs Row")
    public Stream<CreateConnOutputNeuronOutputRow.CreateResult> createConnOutputNeuronOutputRow(@Name("from_id") String from_id,
                                                                                            @Name("to_id") String to_id,
                                                                                            @Name("outputbyrowid") String outputbyrowid,
                                                                                            @Name("value") int value
    ) {
        try (Transaction tx = db.beginTx()) {

            tx.execute("MATCH (n1:Neuron {{id: $from_id,type:'output'}})\n" +
                    "                       MATCH (n2:Row {{id: $to_id,type:'outputsRow'}})\n" +
                    "                       CREATE (n1)-[:CONTAINS {{output: $value,id:$outputbyrowid}}]->(n2)");
            return Stream.of(new CreateConnOutputNeuronOutputRow.CreateResult("ok"));

        } catch (Exception e) {

            return Stream.of(new CreateConnOutputNeuronOutputRow.CreateResult("ko"));
        }
    }

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

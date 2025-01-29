package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateConnOutputRow {
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db; //référence a la base de données neo4J pour éxécuter les requêtes et manipuler des graphes


    // Pour utiliser call nn.createNeuron("123","0","input","sotfmax")
    @Procedure(name = "nn.createConnOutputRow",mode = Mode.WRITE)
    //mode WRITE car va modifier la base de données
    @Description("Creates connection between outputs and node Row")
    public Stream<CreateConnOutputRow.CreateResult> createConnOutputRow(@Name("from_id") String from_id,
                                                          @Name("to_id") String to_id,
                                                          @Name("inputfeatureid") String inputfeatureid,
                                                          @Name("value") int value
    ) {
        try (Transaction tx = db.beginTx()) {

            tx.execute("MATCH (n1:Row {{id: $from_id,type:'inputsRow'}})\n" +
                    "                    MATCH (n2:Neuron {{id: $to_id,type:'input'}})\n" +
                    "                    CREATE (n1)-[:CONTAINS {{output: $value,id:$inputfeatureid}}]->(n2)");
            return Stream.of(new CreateConnOutputRow.CreateResult("ok"));

        } catch (Exception e) {

            return Stream.of(new CreateConnOutputRow.CreateResult("ko"));
        }
    }

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

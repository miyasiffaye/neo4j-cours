package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateConnInputNeuronInputRow {
    @Context
    public Log log; // permet de loguer des messages
    @Context
    public GraphDatabaseService db; // référence à la base de données Neo4j pour exécuter des requêtes et manipuler des graphes


    @Procedure(name = "nn.createConnInputNeuronInputRow", mode = Mode.WRITE)
    @Description("Creates connection between inputs neurons and inputs Row")
    public Stream<CreateConnInputNeuronInputRow.CreateResult> createConnInputNeuronInputRow(
            @Name("from_id") String from_id,
            @Name("to_id") String to_id,
            @Name("inputfeatureid") String inputfeatureid,
            @Name("value") long value
    ) {
        try (Transaction tx = db.beginTx()) {

            // Requête Cypher corrigée avec concaténation de chaînes
            String cypherQuery = "MATCH (n1:Row {id: '" + from_id + "', type: 'inputsRow'})\n" +
                    "MATCH (n2:Neuron {id: '" + to_id + "', type: 'input'})\n" +
                    "CREATE (n1)-[:CONTAINS {output: " + value + ", id: '" + inputfeatureid + "'}]->(n2)";

            // Exécuter la requête Cypher
            tx.execute(cypherQuery);
            tx.commit(); // Assurez-vous que la transaction est bien engagée

            // Si tout se passe bien, retour "ok"
            return Stream.of(new CreateConnInputNeuronInputRow.CreateResult("ok"));

        } catch (Exception e) {
            // Enregistrer l'erreur dans les logs Neo4j
            log.error("Error in createConnInputNeuronInputRow procedure: " + e.getMessage(), e);
            // Retourner "ko" en cas d'erreur
            return Stream.of(new CreateConnInputNeuronInputRow.CreateResult("ko"));
        }
    }

    public static class CreateResult {
        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

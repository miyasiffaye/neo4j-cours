package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateConnOutputNeuronOutputRow {
    @Context
    public Log log; // permet de loguer des messages
    @Context
    public GraphDatabaseService db; // référence à la base de données Neo4j pour exécuter des requêtes et manipuler des graphes


    @Procedure(name = "nn.createConnOutputNeuronOutputRow", mode = Mode.WRITE)
    @Description("Creates connection between output neurons and output Row")
    public Stream<CreateConnOutputNeuronOutputRow.CreateResult> createConnOutputNeuronOutputRow(
            @Name("from_id") String from_id,
            @Name("to_id") String to_id,
            @Name("outputbyrowid") String outputbyrowid,
            @Name("value") long value
    ) {
        try (Transaction tx = db.beginTx()) {

            // Requête Cypher corrigée avec concaténation de chaînes
            String cypherQuery = "MATCH (n1:Neuron {id: '" + from_id + "', type: 'output'})\n" +
                    "MATCH (n2:Row {id: '" + to_id + "', type: 'outputsRow'})\n" +
                    "CREATE (n1)-[:CONTAINS {output: " + value + ", id: '" + outputbyrowid + "'}]->(n2)";

            // Exécution de la requête Cypher
            tx.execute(cypherQuery);
            tx.commit(); // Assurez-vous que la transaction est bien engagée

            // Si tout se passe bien, retour "ok"
            return Stream.of(new CreateConnOutputNeuronOutputRow.CreateResult("ok"));

        } catch (Exception e) {
            // Enregistrer l'erreur dans les logs Neo4j
            log.error("Error in createConnOutputNeuronOutputRow procedure: " + e.getMessage(), e);
            // Retourner "ko" en cas d'erreur
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

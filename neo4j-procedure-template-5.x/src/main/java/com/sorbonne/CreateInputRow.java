package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateInputRow {
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.createInputRow", mode = Mode.WRITE)
    @Description("Creates a node Row of input type for each batch entry")
    public Stream<CreateInputRow.CreateResult> createInputRow(@Name("id") String id) {

        try (Transaction tx = db.beginTx()) {

            // Requête Cypher avec concaténation de chaînes (sans utiliser `parameters`)
            String cypherQuery = "CREATE (n:Row {id: '" + id + "', type: 'inputsRow'})";

            // Exécution de la requête Cypher
            tx.execute(cypherQuery);
            tx.commit(); // Assurez-vous que la transaction est bien engagée

            // Si tout se passe bien, retour "ok"
            return Stream.of(new CreateInputRow.CreateResult("ok"));

        } catch (Exception e) {
            // Enregistrer l'erreur dans les logs Neo4j
            log.error("Error in createInputRow procedure: " + e.getMessage(), e);
            // Retourner "ko" en cas d'erreur
            return Stream.of(new CreateInputRow.CreateResult("ko"));
        }
    }

    public static class CreateResult {
        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

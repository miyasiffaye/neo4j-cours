package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateOutputRow {
    @Context
    public Log log; // permet de loguer des messages
    @Context
    public GraphDatabaseService db; // référence à la base de données Neo4j pour exécuter des requêtes et manipuler des graphes

    @Procedure(name = "nn.createOutputRow", mode = Mode.WRITE)
    @Description("Creates a node Row of output type for each batch entry")
    public Stream<CreateOutputRow.CreateResult> createOutputRow(@Name("id") String id) {

        try (Transaction tx = db.beginTx()) {

            // Requête Cypher corrigée avec concaténation de chaînes
            String cypherQuery = "CREATE (n:Row {id: '" + id + "', type: 'outputsRow'})";

            // Exécution de la requête Cypher
            tx.execute(cypherQuery);
            tx.commit(); // Assurez-vous que la transaction est bien engagée

            // Si tout se passe bien, retour "ok"
            return Stream.of(new CreateOutputRow.CreateResult("ok"));

        } catch (Exception e) {
            // Enregistrer l'erreur dans les logs Neo4j
            log.error("Error in createOutputRow procedure: " + e.getMessage(), e);
            // Retourner "ko" en cas d'erreur
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

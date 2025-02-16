package com.sorbonne;

import org.junit.jupiter.api.*;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import static org.junit.jupiter.api.Assertions.*;

public class CreateOutputsRowTest {

    private static Neo4j embeddedDatabaseServer;
    private static Driver driver;

    @BeforeAll
    static void initializeNeo4j() {
        // Démarrage d'une instance Neo4j en mémoire avec la procédure stockée
        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withProcedure(CreateOutputsRow.class)
                .build();
        driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), Config.defaultConfig());
    }

    @AfterAll
    static void closeNeo4j() {
        driver.close();
        embeddedDatabaseServer.close();
    }

    @Test
    void shouldCreateOutputsRow() {
        try (Session session = driver.session()) {
            // Exécuter la procédure stockée pour créer un nœud
            session.run("CALL nn.createOutputsRow('123')");

            // Vérifier si le nœud a bien été créé
            Record record = session.run("""
                MATCH (n:Row {id: '123', type: 'outputsRow'}) 
                RETURN count(n) AS count
            """).single();

            assertEquals(1, record.get("count").asInt(), "Le nœud outputsRow doit être créé !");
        }
    }
}

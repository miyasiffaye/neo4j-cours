package com.sorbonne;

import org.junit.jupiter.api.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.driver.*;

import static org.junit.jupiter.api.Assertions.*;

class CreateInputsRowTest {

    private static Neo4j embeddedDatabaseServer;
    private static Driver driver;

    @BeforeAll
    static void initializeNeo4j() {
        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withProcedure(CreateInputsRow.class) // Charge la procédure stockée
                .build();
        driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), Config.defaultConfig());
    }

    @AfterAll
    static void closeNeo4j() {
        driver.close();
        embeddedDatabaseServer.close();
    }

    @Test
    void shouldCreateInputsRow() {
        try (Session session = driver.session()) {
            // Appel de la procédure stockée avec un ID spécifique
            Result result = session.run("CALL nn.createInputsRow($id)", Values.parameters("id", "test-123"));

            // Vérifie si la procédure a retourné "ok"
            assertTrue(result.hasNext());
            String response = result.next().get("result").asString();
            assertEquals("ok", response);

            // Vérifie si le nœud a bien été créé dans la base de données
            Result nodeCheck = session.run("MATCH (n:Row {id: 'test-123', type: 'inputsRow'}) RETURN count(n) AS count");
            assertTrue(nodeCheck.hasNext());
            assertEquals(1, nodeCheck.next().get("count").asInt());
        }
    }
}

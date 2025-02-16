package com.sorbone;

import com.sorbonne.CreateConnection;
import org.junit.jupiter.api.*;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.internal.value.RelationshipValue;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreateConnectionTest {

    private Neo4j embeddedDatabaseServer;
    private Driver driver;
    private Session session;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .withProcedure(CreateConnection.class)
                .build();
        driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());
        session = driver.session();
    }

    @AfterAll
    void closeNeo4j() {
        if (session != null) {
            session.close();
        }
        if (driver != null) {
            driver.close();
        }

        this.embeddedDatabaseServer.close();
    }

    @Test
    void testCreateConnection() {
        session.run("CREATE (n:Neuron {id: '1-1'})");
        session.run("CREATE (n:Neuron {id: '2-1'})");

        String creationResult = session
                .run("CALL nn.createConnection('1-1', '2-1', 55.55)")
                .single().get("result").asString();
        assertEquals("Success", creationResult);

        List<Record> matchResult = session.run("MATCH ({id: '1-1'})-[c]-({id: '2-1'}) RETURN c").list();
        assertEquals(1, matchResult.size());
        RelationshipValue connection = (RelationshipValue) matchResult.get(0).get("c");
        assertEquals(55.55, connection.get("weight").asDouble());
    }
}
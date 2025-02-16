package com.sorbonne;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.value.RelationshipValue;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.driver.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConnectOutputsRowToNeuronTest {

    private Neo4j neo4j;
    private Driver driver;

    @BeforeEach
    void setUp() {

        this.neo4j = Neo4jBuilders.newInProcessBuilder()
                .withProcedure(ConnectOutputsRowToNeuron.class)
                .build();

        this.driver = GraphDatabase.driver(neo4j.boltURI(), AuthTokens.none());
    }

    @AfterEach
    void tearDown() {

        if (driver != null) {
            driver.close();
        }
        if (neo4j != null) {
            neo4j.close();
        }
    }

    @Test
    void testConnectOutputsRowToNeuron() {
        try (Session session = driver.session()) {

            session.run("CREATE (n:Neuron {id: '1-2', type:'output'})");
            session.run("CREATE (r:Row {id: '3', type:'outputsRow'})");

            session.run("CALL nn.connectOutputsRowToNeuron(1, 2, 3)");

            List<Record> matchResult = session.run("MATCH (:Neuron {id: '1-2'})-[c:CONTAINS]-(:Row {id: '3'}) RETURN c").list();
            assertEquals(1, matchResult.size());
            RelationshipValue connection = (RelationshipValue) matchResult.get(0).get("c");
            assertEquals("3_2", connection.get("id").asString());
            assertEquals(0.0, connection.get("output").asDouble(), 0.001);
        }
    }
}

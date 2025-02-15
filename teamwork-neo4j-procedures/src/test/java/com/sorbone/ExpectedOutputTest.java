package com.sorbonne;

import org.junit.jupiter.api.*;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExpectedOutputTest {

    private static Neo4j embeddedDatabaseServer;
    private static Driver driver;

    @BeforeAll
    static void setup() {

        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withProcedure(ExpectedOutput.class)
                .build();


        driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), Config.defaultConfig());
    }

    @AfterAll
    static void tearDown() {
        driver.close();
        embeddedDatabaseServer.close();
    }

    @Test
    void testExpectedOutputProcedure() {

        try (Session session = driver.session()) {
            session.run("CREATE (:Neuron {id: 'N1', type: 'output', expected_output: 0.9})");
            session.run("CREATE (:Neuron {id: 'N2', type: 'output', expected_output: 0.7})");
            session.run("CREATE (:Neuron {id: 'N3', type: 'hidden', expected_output: 0.5})"); // Ne doit pas être retourné
        }


        try (Session session = driver.session()) {
            List<Record> result = session.run("CALL nn.expectedOutput()").list();


            assertEquals(2, result.size());

            Map<String, Object> neuron1 = result.get(0).asMap();
            assertEquals("N1", neuron1.get("id"));
            assertEquals(0.9, (double) neuron1.get("expected"), 0.01);

            Map<String, Object> neuron2 = result.get(1).asMap();
            assertEquals("N2", neuron2.get("id"));
            assertEquals(0.7, (double) neuron2.get("expected"), 0.01);
        }
    }
}

package com.sorbonne;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.driver.*;
import org.neo4j.graphdb.Transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.neo4j.driver.Values.parameters;

public class ConnectInputsRowToNeuronTest {

    private Neo4j neo4j;
    private Driver driver;

    @BeforeEach
    void setUp() {

        this.neo4j = Neo4jBuilders.newInProcessBuilder()
                .withProcedure(ConnectInputsRowToNeuron.class)
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
    void testConnectInputsRowToNeuron() {
        try (Session session = driver.session()) {
            // Call procédure `connectInputsRowToNeuron` avec un réseau de 3 couches et un batch size de 2
            session.run("CALL nn.connectInputsRowToNeuron([3, 5, 2], 2)");

        }
    }
}

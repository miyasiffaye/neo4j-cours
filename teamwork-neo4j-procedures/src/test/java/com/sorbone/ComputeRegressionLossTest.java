package com.sorbone;

import com.sorbonne.ComputeRegressionLoss;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComputeRegressionLossTest {

    private Neo4j embeddedDatabaseServer;
    private Driver driver;
    private Session session;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .withProcedure(ComputeRegressionLoss.class)
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
    void testComputeRegressionLoss() {
        session.run("CREATE (:Neuron {id:1, type:'output'})-[:CONTAINS {output:0.5, expected_output:0.3}]->(:Row {type:'outputsRow'})");
        session.run("CREATE (:Neuron {id:2, type:'output'})-[:CONTAINS {output:0.2, expected_output:0.1}]->(:Row {type:'outputsRow'})");
        session.run("CREATE (:Neuron {id:3, type:'output'})-[:CONTAINS]->(:Row {type: 'outputsRow'})");

        Record lossResult = session
                .run("CALL nn.computeRegressionLoss()")
                .single();
        assertEquals("Success", lossResult.get("status").asString());

        // AVG((predicted - expected)^2)
        // (0.2^2 + 0.1^2 + 0) / 3
        assertEquals(0.016, lossResult.get("loss").asDouble(), 0.001);
    }
}
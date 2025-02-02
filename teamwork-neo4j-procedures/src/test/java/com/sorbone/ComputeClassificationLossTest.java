package com.sorbone;

import com.sorbonne.ComputeClassificationLoss;
import com.sorbonne.CreateNeuron;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComputeClassificationLossTest {

    private Neo4j embeddedDatabaseServer;
    private Driver driver;
    private Session session;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .withProcedure(ComputeClassificationLoss.class)
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
    void testComputeClassificationLoss() {
        session.run("CREATE (:Neuron {id:1, type:'output'})-[:CONTAINS {output:0.5, expected_output:0.3}]->(:Row {type:'outputsRow'})");
        session.run("CREATE (:Neuron {id:2, type:'output'})-[:CONTAINS {output:0.2, expected_output:0.1}]->(:Row {type:'outputsRow'})");
        session.run("CREATE (:Neuron {id:3, type:'output'})-[:CONTAINS]->(:Row {type: 'outputsRow'})");

        Record lossResult = session
                .run("CALL nn.computeClassificationLoss()")
                .single();
        assertEquals("Success", lossResult.get("status").asString());

        // SUM(-expected * LOG(predicted + epsilon) - (1 - expected) * LOG(1 - predicted + epsilon))
        // -0.3 * log(0.5 + 1e-10) - (1 - 0.3) * log(1 - 0.5 + 1e-10) - 0.1 * log(0.2 + 1e-10) - (1 - 0.1) * log(1 - 0.2 + 1e-10) ~ 1.054
        assertEquals(1.054, lossResult.get("result").asDouble(), 0.001);
    }
}
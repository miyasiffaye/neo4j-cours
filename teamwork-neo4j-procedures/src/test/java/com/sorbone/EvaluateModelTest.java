
package com.sorbone;

import com.sorbonne.EvaluateModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EvaluateModelTest {

    private Neo4j embeddedDatabaseServer;
    private Driver driver;
    private Session session;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .withProcedure(EvaluateModel.class)
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
    void testEvaluateModel() {
        session.run("CREATE (:Neuron {id:'1', type:'output', output: 1.2})");
        session.run("CREATE (:Neuron {id:'2', type:'output', output: 2.4})");

        List<Record> result = session.run("CALL nn.evaluateModel()").list();
        assertEquals(2, result.size());

        Record node1 = result.get(0);
        Record node2 = result.get(1);

        assertEquals("1", node1.get("id").asString());
        assertEquals(1.2, node1.get("predicted").asDouble(), 0.001);
        assertEquals("2", node2.get("id").asString());
        assertEquals(2.4, node2.get("predicted").asDouble(), 0.001);
    }
}
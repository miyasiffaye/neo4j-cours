package com.sorbone;

import com.sorbonne.CreateNeuron;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreateNeuronTest {

    private Neo4j embeddedDatabaseServer;
    private Driver driver;
    private Session session;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .withProcedure(CreateNeuron.class)
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
    void testCreateNeuron() {
        String creationResult = session
                .run("CALL nn.createNeuron(123, 0, \"input\",\"softmax\")")
                .single().get("result").asString();
        assertEquals("Success", creationResult);

        List<Record> matchResult = session.run("MATCH (n) RETURN n").list();
        assertEquals(1, matchResult.size());
        NodeValue neuron = (NodeValue) matchResult.get(0).get("n");
        assertEquals(123, neuron.get("id").asInt());
        assertEquals(0, neuron.get("layer").asInt());
        assertEquals("input", neuron.get("type").asString());
        assertEquals("softmax", neuron.get("activation_function").asString());
    }
}
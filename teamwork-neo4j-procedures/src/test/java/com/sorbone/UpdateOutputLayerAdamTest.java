package com.sorbonne;

import org.junit.jupiter.api.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import static org.junit.jupiter.api.Assertions.*;

public class UpdateOutputLayerAdamTest {

    private Neo4j neo4j;
    private GraphDatabaseService db;

    @BeforeEach
    void setUp() {

        neo4j = Neo4jBuilders.newInProcessBuilder()
                .withProcedure(UpdateOutputLayerAdam.class)
                .build();
        db = neo4j.defaultDatabaseService();
    }

    @AfterEach
    void tearDown() {

        neo4j.close();
    }

    @Test
    void testUpdateOutputLayerAdam() {

        try (Transaction tx = db.beginTx()) {
            tx.execute("CREATE (input:Neuron {type: 'input', id: 'input1', output: 1.0})");
            tx.execute("CREATE (hidden:Neuron {type: 'hidden', id: 'hidden1', output: 0.5, activation_function: 'relu', bias: 0.5})");
            tx.execute("CREATE (output:Neuron {type: 'output', id: 'output1', output: 0.7, activation_function: 'sigmoid', bias: 0.5})");
            tx.execute("CREATE (row_output:Row {type: 'outputsRow', id: 'row2', output: 0.7, expected_output: 1.0})");
            tx.execute("CREATE (input)-[:CONNECTED_TO {weight: 0.7}]->(hidden)");
            tx.execute("CREATE (hidden)-[:CONNECTED_TO {weight: 0.5}]->(output)");
            tx.execute("CREATE (output)-[:CONTAINS]->(row_output)");
            tx.commit();
        }


        try (Transaction tx = db.beginTx()) {
            tx.execute("CALL nn.updateOutputLayerAdam(0.001, 0.9, 0.999, 1e-8, 1)");
            tx.commit();
        }

        try (Transaction tx = db.beginTx()) {
            Result result = tx.execute("MATCH (n:Neuron {id: 'output1'}) RETURN n.bias AS bias");
            assertTrue(result.hasNext(), "The output neuron should exist");
            Double bias = (Double) result.next().get("bias");
            assertNotNull(bias, "Output neuron bias should not be zero");
            tx.commit();
        }
    }
}

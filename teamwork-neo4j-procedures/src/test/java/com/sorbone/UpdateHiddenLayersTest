package com.sorbonne;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpdateHiddenLayersTest {

    private Neo4j neo4j;
    private GraphDatabaseService db;

    @BeforeEach
    void setUp() {
        // Démarrez une instance Neo4j embarquée avec la procédure enregistrée
        neo4j = Neo4jBuilders.newInProcessBuilder()
                .withProcedure(UpdateHiddenLayers.class)
                .build();
        db = neo4j.defaultDatabaseService();
    }

    @AfterEach
    void tearDown() {
        // Arrêtez l'instance Neo4j après chaque test
        neo4j.close();
    }

    @Test
    void testUpdateHiddenLayers() {
        // Arrange : Créez des données de test dans la base de données
        try (Transaction tx = db.beginTx()) {
            tx.execute("CREATE (n1:Neuron {type: 'hidden', activation_function: 'relu', output: 1.0, gradient: 0.0, bias: 0.0})");
            tx.execute("CREATE (n2:Neuron {type: 'hidden', activation_function: 'relu', output: 0.5, gradient: 0.0, bias: 0.0})");
            tx.execute("CREATE (n1)-[:CONNECTED_TO {weight: 0.5, m: 0.0, v: 0.0}]->(n2)");
            tx.commit();
        }

        // Act : Appelez la procédure stockée
        try (Transaction tx = db.beginTx()) {
            Map<String, Object> params = new HashMap<>();
            params.put("learning_rate", 0.01);
            params.put("beta1", 0.9);
            params.put("beta2", 0.999);
            params.put("epsilon", 1e-8);
            params.put("t", 1L);

            tx.execute("CALL nn.updateHiddenLayers($learning_rate, $beta1, $beta2, $epsilon, $t)", params);
            tx.commit();
        }
    }
}

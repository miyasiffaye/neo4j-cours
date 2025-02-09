package com.sorbonne;

import org.junit.jupiter.api.*;
import org.neo4j.harness.*;
//import org.neo4j.graphdb.*;
import org.neo4j.driver.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InitializeAdamParametersTest {

    private Neo4j embeddedDatabaseServer;
    private Driver driver;

    @BeforeAll
    void setup() {
        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withProcedure(InitializeAdamParameters.class) // Enregistrer la classe contenant les procédures
                .build();
        driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());
    }

    @AfterAll
    void tearDown() {
        driver.close();
        embeddedDatabaseServer.close();
    }

    @Test
    void shouldInitializeAdamConnections() {
        try (Session session = driver.session()) {
            // Création de connexions fictives
            session.run("CREATE (a:Neuron {id: 'n1'}), (b:Neuron {id: 'n2'})");
            session.run("MATCH (a:Neuron {id: 'n1'}), (b:Neuron {id: 'n2'}) CREATE (a)-[:CONNECTED_TO]->(b)");

            // Exécuter la procédure pour initialiser les paramètres Adam des connexions
            var result = session.run("CALL nn.initialize_adam_connections()");

            // Vérifier que la procédure a bien été exécutée
            assertTrue(result.hasNext());
            assertEquals("Adam parameters initialized for connections!", result.next().get("message").asString());

            // Vérifier que les propriétés ont bien été ajoutées aux relations
            var check = session.run("MATCH ()-[r:CONNECTED_TO]->() RETURN r.m, r.v");
            assertTrue(check.hasNext());
            var record = check.next();
            assertEquals(0.0, record.get("r.m").asDouble());
            assertEquals(0.0, record.get("r.v").asDouble());
        }
    }

    @Test
    void shouldInitializeAdamNeurons() {
        try (Session session = driver.session()) {
            // Création de neurones fictifs
            session.run("CREATE (:Neuron {id: 'n1'}), (:Neuron {id: 'n2'})");

            // Exécuter la procédure pour initialiser les paramètres Adam des neurones
            var result = session.run("CALL nn.initialize_adam_neurons()");

            // Vérifier que la procédure a bien été exécutée
            assertTrue(result.hasNext());
            assertEquals("Adam parameters initialized for neurons!", result.next().get("message").asString());

            // Vérifier que les propriétés ont bien été ajoutées aux neurones
            var check = session.run("MATCH (n:Neuron) RETURN n.m_bias, n.v_bias");
            assertTrue(check.hasNext());
            while (check.hasNext()) {
                var record = check.next();
                assertEquals(0.0, record.get("n.m_bias").asDouble());
                assertEquals(0.0, record.get("n.v_bias").asDouble());
            }
        }
    }
}

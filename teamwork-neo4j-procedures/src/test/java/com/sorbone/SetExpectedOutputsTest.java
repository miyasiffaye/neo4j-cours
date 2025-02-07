package com.sorbonne;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.driver.*;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.neo4j.driver.Values.parameters;

public class SetExpectedOutputsTest {

    private Neo4j neo4j;
    private Driver driver;

    @BeforeEach
    void initializeNeo4j() {
        this.neo4j = Neo4jBuilders.newInProcessBuilder()
                .withProcedure(SetExpectedOutputs.class)
                .build();
        this.driver = GraphDatabase.driver(neo4j.boltURI(), Config.builder().withoutEncryption().build());
    }

    @AfterEach
    void closeNeo4j() {
        this.driver.close();
        this.neo4j.close();
    }

    @Test
    void shouldSetExpectedOutputs() {
        try (var session = driver.session()) {
            // Création des neurones de sortie et des relations
            session.run("CREATE (:Neuron {id: '2-0', type: 'output'})");
            session.run("CREATE (:Row {id: '0', type: 'outputsRow'})");
            session.run("MATCH (n:Neuron {id: '2-0'}), (r:Row {id: '0'}) " +
                    "CREATE (n)-[:CONTAINS {id: '0_0'}]->(r)");

            // Appel de la procédure
            session.run("CALL nn.setExpectedOutputs([ {expected_outputs: $outputs} ], 2)",
                    parameters("outputs", singletonMap("0", 0.8)));

            // Vérification que la propriété expected_output est bien mise à jour
            var result = session.run("MATCH (:Neuron {id: '2-0'})-[r:CONTAINS {id: '0_0'}]->(:Row {id: '0'}) " +
                    "RETURN r.expected_output AS expected_output");

            assertThat(result.single().get("expected_output").asDouble()).isEqualTo(0.8);
        }
    }
}

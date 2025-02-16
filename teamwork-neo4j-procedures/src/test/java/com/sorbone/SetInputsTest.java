package com.sorbonne;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.driver.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SetInputsTest {

    private Neo4j neo4j;
    private Driver driver;

    @BeforeEach
    void initializeNeo4j() {
        this.neo4j = Neo4jBuilders.newInProcessBuilder()
                .withProcedure(SetInputs.class) // Charger la procédure
                .build();
        this.driver = GraphDatabase.driver(neo4j.boltURI(), Config.builder().withoutEncryption().build());
    }

    @AfterEach
    void closeNeo4j() {
        this.driver.close();
        this.neo4j.close();
    }

    @Test
    void shouldSetInputsSuccessfully() {
        try (var session = driver.session()) {
            // Création des nœuds Row et Neuron
            session.run("CREATE (:Row {type: 'inputsRow', id: '0'})");
            session.run("CREATE (:Neuron {type: 'input', id: '0-0'})");

            // Création de la relation CONTAINS
            session.run("""
                MATCH (row:Row {id: '0'}), (neuron:Neuron {id: '0-0'})
                CREATE (row)-[:CONTAINS {id: '0_0', output: 0.0}]->(neuron)
            """);

            // Exécution de la procédure avec un dataset
            session.run("CALL nn.setInputs($dataset)", Map.of(
                    "dataset", List.of(Map.of("inputs", List.of(0.5, 1.0, 1.5)))
            ));

            // Vérification de la mise à jour de la relation CONTAINS
            var result = session.run("""
                MATCH (row:Row {id: '0'})-[r:CONTAINS]->(neuron:Neuron {id: '0-0'})
                RETURN r.output AS output
            """);

            // Vérifier si la sortie a bien été normalisée (1.0 car max = 1.5)
            assertThat(result.single().get("output").asDouble()).isEqualTo(0.3333333333333333);
        }
    }
}

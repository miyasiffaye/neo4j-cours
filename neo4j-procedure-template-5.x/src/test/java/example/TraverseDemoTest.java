package example;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Value;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TraverseDemoTest {

    private Neo4j embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() throws IOException {

        var sw = new StringWriter();
        try (var in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/movie.cypher")))) {
            in.transferTo(sw);
            sw.flush();
        }

        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
            .withProcedure(TraverseDemo.class)
            .withFixture(sw.toString())
            .build();
    }

    @AfterAll
    void closeNeo4j() {
        this.embeddedDatabaseServer.close();
    }

    @Test
    void findKeanuReevesCoActors() {

        try(
                var driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());
                var session = driver.session()
        ) {

            // language=cypher
            var names = session.run("""
                    match (keanu:Person {name:'Keanu Reeves'})-[*1..2]-(coactors:Person)
                    with coactors.name as names order by names
                    return distinct names
                    """)
                .stream()
                    .map(r -> r.get("names"))
                    .map(Value::asString)
                    .toList();

            // language=cypher
            var records = session.run("call travers.findCoActors('Keanu Reeves')").list();

            var coActorNames = records.stream()
                    .map(r -> r.get("node"))
                    .map(node -> node.get("name"))
                    .map(Value::asString)
                    .sorted()
                    .toList();
            assertThat(coActorNames).hasSize(names.size());
            assertThat(coActorNames).containsAll(names);
        }
    }
}

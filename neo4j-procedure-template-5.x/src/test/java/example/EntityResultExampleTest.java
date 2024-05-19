package example;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EntityResultExampleTest {

	private Neo4j embeddedDatabaseServer;

	@BeforeAll
	void initializeNeo4j() {
		this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
			.withDisabledServer()
			.withFixture("""
				    UNWIND range(1, 5) AS i
				    WITH i CREATE (n:SomeNode {idx: i})
				""")
			.withProcedure(EntityResultExample.class)
			.build();
	}

	@AfterAll
	void closeNeo4j() {
		this.embeddedDatabaseServer.close();
	}

	@Test
	void allNodesShouldWork() {
		try (
			var driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());
			var session = driver.session()
		) {
			var indizes = session.run("CALL example.allnodes YIELD node")
				.stream().map(r -> r.get("node").get("idx").asInt());

			assertThat(indizes).hasSize(5)
				.containsExactly(1, 2, 3, 4, 5);
		}
	}
}
package com.sorbonne;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

/*  Python code without procedure:
       tx.run("""
                        MATCH (n1:Neuron {id: $from_id})
                        MATCH (n2:Neuron {id: $to_id})
                        CREATE (n1)-[:CONNECTED_TO {weight: $weight}]->(n2)
                    """, from_id=f"{layer_index}-{i}", to_id=f"{layer_index + 1}-{j}",
       weight=weight)
 */

/* Python code with procedure
    tx.run(
        "call nn.createConnection($from_id,$to_id,$weight)",
        from_id=f"{layer_index}-{i}", to_id=f"{layer_index + 1}-{j}",
        $weight=weight)
 */
public class CreateConnection {
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.createConnection",mode = Mode.WRITE)
    @Description("Creates a connection between two neurons")
    public Stream<CreateResult> createConnection(@Name("from_id") Long fromId,
                                                 @Name("to_id") Long toId,
                                                 @Name("weight") Double weight) {
        try (Transaction tx = db.beginTx()) {
            tx.execute("MATCH (n1:Neuron {id: " + fromId + "})\n" +
                        "MATCH (n2:Neuron {id: " +  toId + "})\n" +
                        "CREATE (n1)-[:CONNECTED_TO {weight: " + weight + "}]->(n2)");
            tx.commit();
            return Stream.of(new CreateResult("Success"));
        } catch (Exception e) {
            return Stream.of(new CreateResult("Failure"));
        }
    }

    public static class CreateResult {
        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

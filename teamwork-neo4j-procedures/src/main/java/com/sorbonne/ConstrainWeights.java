package com.sorbonne;
import org.neo4j.procedure.*;
import org.neo4j.graphdb.*;
public class ConstrainWeights {

    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.constrainWeights", mode = Mode.WRITE)
    @Description("Constrain the weights of connections to be between -1.0 and 1.0")
    public void constrainWeights() {
        try (Transaction tx = db.beginTx()) {
            tx.execute("""
                MATCH ()-[r:CONNECTED_TO]->()
                SET r.weight = CASE
                    WHEN r.weight > 1.0 THEN 1.0
                    WHEN r.weight < -1.0 THEN -1.0
                    ELSE r.weight
                END
            """);
            tx.commit();
        }
    }
}

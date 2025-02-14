package com.sorbonne;

//import org.eclipse.jetty.util.Index;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class InitAdamParameters {
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.initAdam", mode = Mode.READ)
    @Description("initialize adam parameters")
    public Stream<CreateResult> init_ad_params() {
        try (Transaction tx = db.beginTx()) {
            tx.execute("MATCH ()-[r:CONNECTED_TO]->()\n" +
                    "  SET r.m = 0.0, r.v = 0.0");
            tx.execute(" MATCH (n:Neuron)\n" +
                    " SET n.m_bias = 0.0, n.v_bias = 0.0");
            tx.commit();
            return Stream.of(new CreateResult("OK"));

        } catch (Exception e) {
            return Stream.of(new CreateResult("KO"));
        }

    }

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}

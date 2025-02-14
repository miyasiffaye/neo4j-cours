package com.sorbonne;
import org.neo4j.procedure.*;
import org.neo4j.graphdb.*;
import java.util.stream.Stream;

public class Classification {

    @Context
    public GraphDatabaseService db;

    public static class MessageResult {
        public String message;

        public MessageResult(String message) {
            this.message = message;
        }
    }
    @Procedure(name = "nn.Classification", mode = Mode.WRITE)
    @Description(" Cross-Entropy Loss for classification")
    public Stream<MessageResult> classification() {
        try(Transaction tx = db.beginTx()){
            tx.execute("""
                    MATCH (output:Neuron {type: 'output'})
                    MATCH (output)-[outputsValues_R:CONTAINS]->(row_for_outputs:Row {type: 'outputsRow'})
                    WITH outputsValues_R,
                    COALESCE(outputsValues_R.output, 0) AS predicted,
                    COALESCE(outputsValues_R.expected_output, 0) AS actual,
                                             1e-10 AS epsilon
                    RETURN SUM(
                                            -actual * LOG(predicted + epsilon) - (1 - actual) * LOG(1 - predicted + epsilon)
                                        ) AS loss""");
            tx.commit();
            return Stream.of(new MessageResult("OK"));

        } catch (Exception e) {
            return Stream.of(new MessageResult("KO"));
        }
    }



}




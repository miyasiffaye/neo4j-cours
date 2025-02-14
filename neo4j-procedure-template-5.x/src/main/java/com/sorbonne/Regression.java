package com.sorbonne;

import org.neo4j.procedure.*;
import org.neo4j.graphdb.*;
import java.util.stream.Stream;
//import java.util.Map;

public class Regression {

    @Context
    public GraphDatabaseService db;

    public static class ResultMessage {
        public String message;
        public ResultMessage(String message) { this.message = message; }
    }
    @Procedure(name = "nn.Regression", mode = Mode.WRITE)
    @Description("Mean Squared Error (MSE) for regression")
    public Stream<ResultMessage> updateNeurons(){

        try(Transaction tx = db.beginTx()){
            tx.execute("""
                    MATCH (output:Neuron {type: 'output'})
                                        MATCH (output)-[outputsValues_R:CONTAINS]->(row_for_outputs:Row {type: 'outputsRow'})
                                        WITH outputsValues_R,
                                             COALESCE(outputsValues_R.output, 0) AS predicted,
                                             COALESCE(outputsValues_R.expected_output, 0) AS actual
                                        RETURN AVG((predicted - actual)^2) AS loss""");
            tx.commit();
            return Stream.of(new ResultMessage("OK"));
        } catch (Exception e) {
            return Stream.of(new ResultMessage("KO"));
        }
    }
}

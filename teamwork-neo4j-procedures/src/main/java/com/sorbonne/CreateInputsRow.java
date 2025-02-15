package com.sorbonne;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.procedure.*;

import java.util.Map;
import java.util.stream.Stream;

public class CreateInputsRow {

    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.createInputsRow", mode = Mode.WRITE)
    @Description("Creates an input row node for a training batch.")
    public Stream<ResultMessage> createInputsRow(
            @Name("id") String id
    ) {
        String query = """
            CREATE (n:Row {
                id: $id,
                type: 'inputsRow'
            })
        """;

        try {
            db.executeTransactionally(query, Map.of("id", id));
            return Stream.of(new ResultMessage("ok"));
        } catch (Exception e) {
            return Stream.of(new ResultMessage("error: " + e.getMessage()));
        }
    }

    public static class ResultMessage {
        public final String result;

        public ResultMessage(String result) {
            this.result = result;
        }
    }
}

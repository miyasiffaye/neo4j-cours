package com.sorbonne;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.procedure.*;
import java.util.Map;
import java.util.stream.Stream;


public class CreateOutputsRow{

    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.createOutputsRow", mode = Mode.WRITE)
    @Description("Crée un nœud de rangée de sortie pour un batch d'entraînement.")
    public Stream<ResultMessage> createOutputsRow(
            @Name("id") String id
    ) {
        String query = """
            CREATE (n:Row {
                id: $id,
                type: 'outputsRow'
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

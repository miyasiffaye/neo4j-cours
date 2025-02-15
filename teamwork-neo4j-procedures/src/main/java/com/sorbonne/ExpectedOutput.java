package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class ExpectedOutput {


    public static class Output {
        public String id;
        public Double expected;

        public Output(String id, Double expected) {
            this.id = id;
            this.expected = expected;
        }
    }

    @Context
    public Transaction tx;


    @Procedure(name = "nn.expectedOutput", mode = Mode.READ)
    @Description("Retourne les ID et valeurs attendues des neurones de type 'output'")
    public Stream<Output> expectedOutput() {
        return tx.execute("MATCH (n:Neuron {type: 'output'}) RETURN n.id AS id, n.expected_output AS expected")
                .stream()
                .map(row -> new Output((String) row.get("id"), (Double) row.get("expected")));
    }
}

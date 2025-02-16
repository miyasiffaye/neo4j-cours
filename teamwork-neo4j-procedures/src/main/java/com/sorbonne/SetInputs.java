package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SetInputs {
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.setInputs", mode = Mode.WRITE)
    @Description("Met à jour les valeurs normalisées des connexions entre Row et Neuron")
    public Stream<Result> setInputs(@Name("dataset") List<Object> dataset) {
        try (Transaction tx = db.beginTx()) {
            for (int rowIndex = 0; rowIndex < dataset.size(); rowIndex++) {
                Object rowObject = dataset.get(rowIndex);
                if (!(rowObject instanceof Map)) continue;

                @SuppressWarnings("unchecked")
                Map<String, Object> row = (Map<String, Object>) rowObject;

                if (!row.containsKey("inputs") || !(row.get("inputs") instanceof List)) continue;

                @SuppressWarnings("unchecked")
                List<Number> rawInputs = (List<Number>) row.get("inputs");

                // Normalisation (hypothèse : fonction normalize à implémenter)
                List<Double> normalizedInputs = normalize(rawInputs);

                for (int i = 0; i < normalizedInputs.size(); i++) {
                    double value = normalizedInputs.get(i);
                    String inputFeatureId = rowIndex + "_" + i;
                    String inputNeuronId = "0-" + i;

                    String query = "MATCH (row:Row {type:'inputsRow', id: $rowid})-"
                            + "[r:CONTAINS {id: $inputfeatureid}]->"
                            + "(inputs:Neuron {type:'input', id: $inputneuronid}) "
                            + "SET r.output = $value";

                    Map<String, Object> params = new HashMap<>();
                    params.put("rowid", String.valueOf(rowIndex));
                    params.put("inputfeatureid", inputFeatureId);
                    params.put("inputneuronid", inputNeuronId);
                    params.put("value", value);

                    tx.execute(query, params);
                }
            }
            tx.commit();
            return Stream.of(new Result("Inputs initialized successfully!"));
        } catch (Exception e) {
            return Stream.of(new Result("Error: " + e.getMessage()));
        }
    }

    // Exemple d'implémentation de la normalisation (à adapter selon le besoin)
    private List<Double> normalize(List<Number> inputs) {
        double max = inputs.stream().mapToDouble(Number::doubleValue).max().orElse(1.0);
        return inputs.stream().map(x -> x.doubleValue() / max).collect(Collectors.toList());
    }

    public static class Result {
        public String message;
        public Result(String message) { this.message = message; }
    }
}

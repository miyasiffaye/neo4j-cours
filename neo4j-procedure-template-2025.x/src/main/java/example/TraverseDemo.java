package example;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

/**
 * Simple demo on how to use the TraversalAPI.
 * Assumes to be running on the Movie graph.
 */
public class TraverseDemo {

    static final Label PERSON = Label.label("Person");

    @Context
    public Transaction tx;

    @Context
    public Log log;

    /**
     * Uses the Traversal API to return all Person fond by a Depth of 2.
     * This could be much easier with a simple Cypher statement, but serves as a demo onl.
     * @param actorName name of the Person node to start from
     * @return Stream of Person Nodes
     */
    @Procedure(name = "travers.findCoActors", mode = Mode.READ)
    @Description("traverses starting from the Person with the given name and returns all co-actors")
    public Stream<CoActorRecord> findCoActors(@Name("actorName") String actorName) {

        Node actor = tx.findNodes(PERSON, "name", actorName)
                .stream()
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

        final Traverser traverse = tx.traversalDescription()
                .depthFirst()
                .evaluator(Evaluators.fromDepth(1))
                .evaluator(Evaluators.toDepth(2))
                .evaluator(Evaluators.includeIfAcceptedByAny(new PathLogger(), new LabelEvaluator(PERSON)))
                .traverse(actor);

        return StreamSupport
                .stream(traverse.spliterator(), false)
                .map(Path::endNode)
                .map(CoActorRecord::new);
    }

    /**
     * See <a href="https://neo4j.com/docs/java-reference/4.2/javadocs/org/neo4j/procedure/Procedure.html">Procedure</a>
     * <blockquote>
     * A procedure must always return a Stream of Records, or nothing. The record is defined per procedure, as a class
     * with only public, non-final fields. The types, order and names of the fields in this class define the format of the
     * returned records.
     * </blockquote>
     * This is a record that wraps one of the valid return types (in this case a {@link Node}.
     */
    public static final class CoActorRecord {

        public final Node node;

        CoActorRecord(Node node) {
            this.node = node;
        }
    }

    /**
     * Miss-using an evaluator to log out the path being evaluated.
     */
    private final class PathLogger implements Evaluator {

        @Override
        public Evaluation evaluate(Path path) {
            log.info(path.toString());
            return Evaluation.EXCLUDE_AND_CONTINUE;
        }
    }

    private record LabelEvaluator(Label label) implements Evaluator {

        @Override
        public Evaluation evaluate(Path path) {
            if (path.endNode().hasLabel(label)) {
                return Evaluation.INCLUDE_AND_CONTINUE;
            } else {
                return Evaluation.EXCLUDE_AND_CONTINUE;
            }
        }
    }
}

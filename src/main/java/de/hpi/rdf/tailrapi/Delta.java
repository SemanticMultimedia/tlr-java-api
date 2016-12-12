package de.hpi.rdf.tailrapi;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Delta} refers to the difference between two
 * mementos version.
 * <p>
 * Given a certain {@link Memento} a Delta contains the
 * added and removed triples from the previous memento to
 * the given one.
 * <p>
 * It can convert itself into a SPARQL query for inserting or deleting data.
 * The used data corresponds to the added and removed triples.<br/>
 * An empty delta produces no queries which are empty strings.
 * <p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class Delta {

    private List<String> addedTriples;

    private List<String> removedTriples;

    /**
     * Instantiates a new Delta.
     */
    public Delta() {
        this.addedTriples = new ArrayList<>();
        this.removedTriples = new ArrayList<>();
    }

    /**
     * Gets added triples.
     *
     * @return the added triples
     */
    public List<String> getAddedTriples() {
        return addedTriples;
    }

    /**
     * Gets removed triples.
     *
     * @return the removed triples
     */
    public List<String> getRemovedTriples() {
        return removedTriples;
    }

    /**
     * Gets an insert query which is build of the triples
     * within the added triples list. Also it appends an empty where clause, to please jena.
     *
     * @param graphUri the graph uri
     * @return the insert query or an empty set if no triples are present
     */
    public String getInsertQuery(String graphUri) {
        if (addedTriples.isEmpty()) return "";
        return "Insert { Graph <" + graphUri + "> {\n" + buildInsertBody(addedTriples) + "} }"
                + "Where {\n" + "}\n";
    }

    /**
     * Gets a delete query which is build of the triples
     * within the removed triples list.
     *
     * @param graphUri the graph uri
     * @return the delete query or an empty string if no triples are present
     */
    public String getDeleteQuery(String graphUri) {
        if (removedTriples.isEmpty()) return "";
        return "Delete { Graph <" + graphUri + "> {\n" + buildDeleteBody(removedTriples, graphUri);
    }

    /* an insert query is easier since we don'nt handle blank nodes */
    private String buildInsertBody(List<String> triples) {
        StringBuilder builder = new StringBuilder();
        for (String triple : triples) {
            builder.append(triple.replace("  ", " ")).append(" ");
        }
        return builder.append("\n").toString();
    }

    /* handle blank nodes within triples which are converted into variables
    * and added to a where clause for binding. This removes the maximum sub graph
    * but it is possible that larger sub graphs are also deleted.
    * TODO prevent sub graphs with more triples from deletion */
    private String buildDeleteBody(List<String> triples, String graph) {
        StringBuilder builder = new StringBuilder();
        List<String> whereClause = new ArrayList<>();

        for (String triple : handleBlankNodes(triples)) {
            builder.append(triple.replace("_:", "?")).append(" ");
            if (triple.contains("?")) whereClause.add(triple);
        }
        builder.append("} }\n");

        /* handle using <> where clause */
        //if (!whereClause.isEmpty())
        builder.append("Where {\n");
        for (String blankNode : whereClause) {
            builder.append(blankNode).append(" ");
        }
        //if (!whereClause.isEmpty())
        builder.append("}\n");

        return builder.toString();
    }

    /**
     * Convert blank nodes in triples into variables for sparql.
     * This the triples should be in NT syntax.
     *
     * @param triples the triples with possible blank nodes
     * @return the list with variables for blank nodes
     */
    static List<String> handleBlankNodes(List<String> triples) {
        List<String> handledTriples = new ArrayList<>();

        for (String triple : triples) {
            handledTriples.add(triple.replace("_:", "?"));
        }

        return handledTriples;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Delta delta = (Delta) o;

        if (!addedTriples.equals(delta.addedTriples)) return false;
        return removedTriples.equals(delta.removedTriples);

    }

    @Override
    public int hashCode() {
        int result = addedTriples.hashCode();
        result = 31 * result + removedTriples.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Delta{\n" +
                "addedTriples=" + addedTriples +
                "\nremovedTriples=" + removedTriples +
                '}';
    }

    /**
     * A convenient method for getting the resulting sparql queries.
     * If booth queries are found their glued together with a ';'
     *
     * @param graph the graph for the query
     * @return the sparql queries
     */
    public String toSparql(String graph) {
        String insert = getInsertQuery(graph);
        String delete = getDeleteQuery(graph);
        if (!insert.isEmpty() && !delete.isEmpty()) {
            return insert + "; " + delete;
        } else {
            return  getInsertQuery(graph) + " " + getDeleteQuery(graph);
        }
    }

}

package de.hpi.rdf.tailrapi;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Delta} refers to the difference between two
 * mementos version.
 *
 * Given a certain {@link Memento} a Delta contains the
 * added and removed triples from the previous memento to
 * the given one.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class Delta {

    private List<String> addedTriples;

    private List<String> removedTriples;

    public Delta() {
        this.addedTriples = new ArrayList<String>();
        this.removedTriples = new ArrayList<String>();
    }

    public List<String> getAddedTriples() {
        return addedTriples;
    }

    public List<String> getRemovedTriples() {
        return removedTriples;
    }

    public String getInsertQuery() {
        return "Insert data {" + buildQueryBody(addedTriples) + "}";
    }


    public String getInsertQuery(String graphUri) {
        return "Insert data { graph <" + graphUri + "> {" + buildQueryBody(addedTriples) + "}}";
    }

    public String getDeleteQuery() {
        return "Delete data {" + buildQueryBody(removedTriples) + "}";
    }

    public String getDeleteQuery(String graphUri) {
        return "Delete data { graph <" + graphUri + "> {" + buildQueryBody(removedTriples) + "}}";
    }

    private String buildQueryBody(List<String> triples) {
        StringBuilder builder = new StringBuilder();
        for (String triple : triples) {
            builder.append(triple).append(" ");
        }
        return builder.toString();
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
        return "Delta{" +
                "addedTriples=" + addedTriples +
                ", removedTriples=" + removedTriples +
                '}';
    }

}

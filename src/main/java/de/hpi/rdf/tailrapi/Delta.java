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

    public String convertToSparql() {
        return "";
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

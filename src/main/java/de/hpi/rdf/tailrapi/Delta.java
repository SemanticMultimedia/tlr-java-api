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
    public String toString() {
        return "Delta{" +
                "addedTriples=" + addedTriples +
                ", removedTriples=" + removedTriples +
                '}';
    }
}

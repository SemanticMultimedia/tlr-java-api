package de.hpi.rdf.tailrapi;

/**
 * This class provides static function to convert
 * Deltas into different formats.
 * <p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class TlrParser {

    /**
     * Convert to basic sparql string.
     *
     * @param d     the delta to convert
     * @param graph the graph
     * @return either a valid sparql query or if the delta is empty an empty string.
     */
    public static String convertToBasicSparql(Delta d, String graph) {
        if (d.getAddedTriples().isEmpty() && d.getRemovedTriples().isEmpty()) {

        }
        return "";
    }



}

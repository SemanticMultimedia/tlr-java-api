package de.hpi.rdf.tailrapi;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@RunWith(Enclosed.class)
public class DeltaTest {

    /* test methods with multiple possible results */
    @RunWith(Parameterized.class)
    public static class DeltaToSparqlTest {
        @Parameterized.Parameters
        public static Collection<Object[]> deltas() {
            Delta dEmpty = new Delta();

            Delta dAdd = new Delta();
            dAdd.getAddedTriples().add("<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .");
            dAdd.getAddedTriples().add("<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> .");

            Delta dRemove = new Delta();
            dRemove.getRemovedTriples().add("<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .");
            dRemove.getRemovedTriples().add("<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> .");

            Delta dMix = new Delta();
            dMix.getAddedTriples().add("<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .");
            dMix.getRemovedTriples().add("<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> .");

            Delta dBlank = new Delta();
            dBlank.getAddedTriples().add("_:b3298438 <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .");
            dBlank.getAddedTriples().add("_:b3298438 <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> .");
            dBlank.getRemovedTriples().add("_:b32984381 <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .");
            dBlank.getRemovedTriples().add("_:b32984383 <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> .");
            dBlank.getRemovedTriples().add("_:b32984383 <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .");
            dBlank.getRemovedTriples().add("_:b32984381 <http://filmontology.org/ontology/1.0/identifier> _:b32984383 .");

            String headInsert = "Insert { Graph <http://filmontology.org> {";
            String headDelete = "Delete { Graph <http://filmontology.org> {";
            String headWhere = "Using <http://filmontology.org> Where {";

            return Arrays.asList(new Object[][]{
                    /* delta : insert query : delete query */
                    {dEmpty, "", ""},
                    {dAdd, headInsert + "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> . "
                            + "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> . }}", ""},
                    {dRemove, "", headDelete + "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> . "
                            + "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> . }}"},
                    {dMix, headInsert + "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> . }}",
                            headDelete + "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> . }}"},
                    {dBlank, headInsert + "_:b3298438 <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> . "
                                + "_:b3298438 <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> . }}",
                            headDelete + "?b32984381 <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> . "
                                + "?b32984383 <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> . "
                                + "?b32984383 <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> . "
                                + "?b32984381 <http://filmontology.org/ontology/1.0/identifier> ?b32984383 . }} "
                            + headWhere + "?b32984381 <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> . "
                                    + "?b32984383 <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> . "
                                    + "?b32984383 <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> . "
                                    + "?b32984381 <http://filmontology.org/ontology/1.0/identifier> ?b32984383 . }"},
            });
        }

        private static String graph = "http://filmontology.org";

        private Delta d;

        private String expectedInsertQuery;

        private String expectedDeleteQuery;

        public DeltaToSparqlTest(Delta d, String expectedInsertQuery, String expectedDeleteQuery) {
            this.d = d;
            this.expectedInsertQuery = expectedInsertQuery;
            this.expectedDeleteQuery = expectedDeleteQuery;
        }


        @Test
        public void convertToBasicSparql() throws Exception {
            String insertQuery = d.getInsertQuery(graph);
            String deleteQuery = d.getDeleteQuery(graph);
            Assert.assertEquals("Insert query differs:", expectedInsertQuery, insertQuery);
            Assert.assertEquals("Delete query differs:", expectedDeleteQuery, deleteQuery);
            System.out.println(d.toSparql(graph));
        }

    }

    /* test simple methods with static behaviour */
    public static class StaticsTests {

        @Test
        public void testHandleBnodes() {
            String bnodeTriple = "_:b3298438 <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .";
            List<String> bnodeList = Arrays.asList(
                    "_:b3298438 <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .",
                    "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> _:b3298438.",
                    "_:b3298438dc <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .",
                    "_:b32984383 <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .",
                    "_:b32984383 <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .",
                    "_:b3298438 <http://filmontology.org/ontology/1.0/internalIdentifier> _:b32984383 .",
                    "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .");

            List<String> result = Delta.handleBlankNodes(Collections.singletonList(bnodeTriple));
            Assert.assertTrue(result.size() == 1);
            Assert.assertEquals(
                    "?b3298438 <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .", result.get(0));

            result = Delta.handleBlankNodes(bnodeList);
            List<String> expectedBlankNodes = Arrays.asList(
                    "?b3298438 <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .",
                    "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> ?b3298438.",
                    "?b3298438dc <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .",
                    "?b32984383 <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .",
                    "?b32984383 <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .",
                    "?b3298438 <http://filmontology.org/ontology/1.0/internalIdentifier> ?b32984383 .",
                    "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/internalIdentifier> \"1\"^^<http://www.w3.org/2001/XMLSchema#long> .");
            Assert.assertTrue(result.size() == expectedBlankNodes.size());
            Assert.assertEquals(expectedBlankNodes, result);
        }

    }
}
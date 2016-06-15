package de.hpi.rdf.tailrapi;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Graph;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by magnus on 01.06.16.
 */
public class TailrClientTest {

    @Test
    public void testTailrClient() throws URISyntaxException, IOException {
        TailrClient tlr = TailrClient.getInstance();

        Assert.assertNotNull(tlr.getTailrUri());

        List<Repository> repos = tlr.getUserRepositories();
        Assert.assertFalse(repos.isEmpty());

        for (Repository repo : repos) {
            List<String> keys = tlr.getRepositoryKeys(repo);
            Assert.assertFalse(keys.isEmpty());

            for (String key : keys) {
                List<Memento> mementos = tlr.getMementos(repo, key);
                Assert.assertFalse(mementos.isEmpty());
            }
        }
    }

    @Test
    public void testTailrGetMementos() throws URISyntaxException, IOException {
        TailrClient tlr = TailrClient.getInstance();

        Repository repo = new Repository("mgns", "dbpediaLive");

        List<String> keys = tlr.getRepositoryKeys(repo);
        Assert.assertFalse(keys.isEmpty());

        for (String key : keys) {
            if (key.startsWith("http://mappings.dbpedia.org/server/ontology"))
            //if (!key.contains("Celine_Dion"))
                continue;

            List<Memento> mementos = tlr.getMementos(repo, key);
            Assert.assertFalse(mementos.isEmpty());

            for (Memento m : mementos) {
                System.out.println(m.getDateTime() + " " + m.getMementoUri());

                try {
                    Graph g = m.resolve();

                    if (g.isEmpty()) {
                        System.out.println("DELETE");
                        tlr.deleteMemento(m);
                    }
                } catch (HttpException httpException) {
                    if (httpException.getResponseCode() == 404) {
                        System.out.println("DELETE 404");
                        tlr.deleteMemento(m);
                    } else {
                        System.out.println(httpException.getResponseCode() + " - " + httpException.getStatusLine());
                    }
                }
            }
        }

    }

    @Test
    public void testTailrGetDelta() throws URISyntaxException, IOException {
        TailrClient tlr = TailrClient.getInstance("http://tailr.s16a.org/", "santifa", "");
        Repository repo = new Repository("santifa", "dwerft");
        DateTime t = new DateTime();
        Memento mem = new Memento(repo, "http://example.org", t);

        Delta d = tlr.getDelta(mem);
        Assert.assertNotNull(d);
        Assert.assertFalse(d.getAddedTriples().isEmpty());
        Assert.assertFalse(d.getRemovedTriples().isEmpty());
    }

    @Test
    public void testDeltaToSparqlConversion() {
        Delta d = new Delta();
        d.getAddedTriples().add("<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> .");
        d.getRemovedTriples().add("<http://filmontology.org/resource/Cast/12312> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://filmontology.org/ontology/1.0/Cast> .");
        d.getRemovedTriples().add("<http://filmontology.org/resource/Cast/984745> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://filmontology.org/ontology/1.0/Cast> .");
        d.getRemovedTriples().add("<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/title> \"Frog King Reloaded\"^^<http://www.w3.org/2001/XMLSchema#string> .");

        Assert.assertEquals("", d.convertToSparql());
    }
}
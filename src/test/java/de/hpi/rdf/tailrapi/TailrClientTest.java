package de.hpi.rdf.tailrapi;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

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
    public void testCreateAndCloseClient() throws URISyntaxException {
        TailrClient tlr = TailrClient.getInstance();
        Assert.assertThat("", is(tlr.getToken()));
        Assert.assertThat("mgns", is(tlr.getUser()));
        Assert.assertThat(new URI("http://tailr.s16a.org/"), is(tlr.getTailrUri()));
        tlr.close();

        tlr = TailrClient.getInstance("http://tailr.s16a.org/", "santifa", "123", false);
        Assert.assertThat("123", is(tlr.getToken()));
        Assert.assertThat("santifa", is(tlr.getUser()));
        Assert.assertThat(new URI("http://tailr.s16a.org/"), is(tlr.getTailrUri()));
        tlr.close();
    }

    @Test(expected = URISyntaxException.class)
    public void testWrongBaseUri() throws URISyntaxException {
        TailrClient.getInstance("mmn:\\ksdfjasa", "", "", false);
    }

    @Test
    public void testPutMemento() throws URISyntaxException, IOException {
        // insert key for test
        TailrClient tlr = TailrClient.getInstance("http://tailr.s16a.org/", "santifa", "", false);
        Repository repo = new Repository("santifa", "dwerft");
        String key = "http://example.org";
        String expected = "<http://filmontology.org/resource/Cast/12312> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://filmontology.org/ontology/1.0/Cast> .";
        String content = "<http://filmontology.org/resource/Cast/12313> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://filmontology.org/ontology/1.0/Cast> .";

        Delta d = tlr.putMemento(repo, key, content);
        Delta expectedDelta = new Delta();
        expectedDelta.getRemovedTriples().add(expected);
        expectedDelta.getAddedTriples().add(content);
        Assert.assertThat(expectedDelta, is(d));
    }

    @Test
    public void testTailrGetDelta() throws URISyntaxException, IOException {
        TailrClient tlr = TailrClient.getInstance("http://tailr.s16a.org/", "santifa", "", false);
        Repository repo = new Repository("santifa", "dwerft");
        DateTime t = new DateTime();
        Memento mem = new Memento(repo, "http://example.org", t);

        Delta d = tlr.getDelta(mem);
        Assert.assertNotNull(d);
        Assert.assertFalse(d.getAddedTriples().isEmpty());
        Assert.assertFalse(d.getRemovedTriples().isEmpty());
    }

    @Test
    public void testTailrGetLatestDelta() throws URISyntaxException, IOException {
        TailrClient tlr = TailrClient.getInstance("http://tailr.s16a.org/", "santifa", "", false);
        Repository repo = new Repository("santifa", "dwerft");
        DateTime t = new DateTime();

        Delta d = tlr.getLatestDelta(repo, "http://example.org");
        Assert.assertNotNull(d);
        Assert.assertFalse(d.getAddedTriples().isEmpty());
        Assert.assertFalse(d.getRemovedTriples().isEmpty());
    }

    @Test
    public void testDeltaToSparqlConversion() {
        String graphUri = "http://example.org/";
        Delta d = new Delta();
        d.getAddedTriples().add("<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> .");
        d.getRemovedTriples().add("<http://filmontology.org/resource/Cast/12312> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://filmontology.org/ontology/1.0/Cast> .");
        d.getRemovedTriples().add("<http://filmontology.org/resource/Cast/984745> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://filmontology.org/ontology/1.0/Cast> .");
        d.getRemovedTriples().add("<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/title> \"Frog King Reloaded\"^^<http://www.w3.org/2001/XMLSchema#string> .");

        Assert.assertEquals("Insert data { graph <" + graphUri + "> {" +
                        "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/identifier> \"3298438\"^^<http://www.w3.org/2001/XMLSchema#int> . }}"
                , d.getInsertQuery(graphUri));
        Assert.assertEquals("Delete data { graph <" + graphUri + "> {"
                + "<http://filmontology.org/resource/Cast/12312> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://filmontology.org/ontology/1.0/Cast> . "
                + "<http://filmontology.org/resource/Cast/984745> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://filmontology.org/ontology/1.0/Cast> . "
                + "<http://filmontology.org/resource/Project/3298438> <http://filmontology.org/ontology/1.0/title> \"Frog King Reloaded\"^^<http://www.w3.org/2001/XMLSchema#string> . }}"
                , d.getDeleteQuery(graphUri));
    }

    @Test
    public void testPutBigFile() throws URISyntaxException, IOException {
        TailrClient tlr = TailrClient.getInstance("http://tailr.s16a.org/", "santifa", "384a7406234809ef76689f8e725259e723926620", true);
        Repository repo = new Repository("santifa", "test");
        String key = "ontology";

        /* load huge file */
        URL file = getClass().getResource("/dwerft-ontology_v2.ttl");

        Model m = ModelFactory.createDefaultModel();
        RDFDataMgr.read(m, file.openStream(), Lang.TTL);
        StringWriter writer = new StringWriter();
        RDFDataMgr.write(writer, m, Lang.NTRIPLES);
        String content = writer.getBuffer().toString();

        Delta d = tlr.putMemento(repo, key, content);
        System.out.println(tlr.prettifyTimemap(tlr.getMementos(repo, "ontology")));
        tlr.deleteMemento(tlr.getLatestMemento(repo, "ontology"));
        System.out.println(tlr.prettifyTimemap(tlr.getMementos(repo, "ontology")));
    }
}
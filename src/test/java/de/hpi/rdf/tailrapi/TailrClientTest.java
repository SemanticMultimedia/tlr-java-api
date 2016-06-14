package de.hpi.rdf.tailrapi;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Graph;
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
}
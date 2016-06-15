package de.hpi.rdf.tailrapi;

import com.google.inject.Inject;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.UnsupportedEncodingException;
import java.net.*;

/**
 * Created by magnus on 01.06.16.
 */
public class Memento {

    private static DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static DateTimeFormatter fmtQS = DateTimeFormat.forPattern("yyyy-MM-dd-HH:mm:ss");

    private Repository repository;
    private String key;
    private DateTime dateTime;

    @Inject
    private TailrClient tlr;

    public Memento(Repository repo, String key, String dateTime) {
        this(repo, key, DateTime.parse(dateTime, fmt));
    }

    public Memento(Repository repo, String key, DateTime dateTime) {
        this.repository = repo;
        this.key = key;
        this.dateTime = dateTime;
    }

    public Graph resolve() throws HttpException, UnsupportedEncodingException, URISyntaxException {

        URI uri = new URI(getMementoUri().toString());

        Graph result = RDFDataMgr.loadGraph(uri.toASCIIString());
        return result;
    }

    public Repository getRepository() {
        return repository;
    }

    public String getKey() {
        return key;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public URI getMementoUri() throws UnsupportedEncodingException, URISyntaxException {
        return new URI(TailrClient.getInstance().getTailrUri().toString() + "api/" + repository.getUser() + "/" + repository.getName() + "?key=" + URLEncoder.encode(key, "UTF8") + "&datetime=" + fmtQS.print(dateTime));
    }

    public URI getMementoUri(URI tailrUri) throws UnsupportedEncodingException, URISyntaxException {
        return new URI(tailrUri.toString() + "api/" + repository.getUser() + "/" + repository.getName() + "?key=" + URLEncoder.encode(key, "UTF8") + "&datetime=" + fmtQS.print(dateTime));
    }
}

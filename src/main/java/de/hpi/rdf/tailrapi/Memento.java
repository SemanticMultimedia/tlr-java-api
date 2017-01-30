package de.hpi.rdf.tailrapi;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.cache.HeaderConstants;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;


/**
 * Created by magnus on 01.06.16.
 */
public class Memento {

    private static DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static DateTimeFormatter fmtQS = DateTimeFormat.forPattern("yyyy-MM-dd-HH:mm:ss");

    private Repository repository;
    private String key;
    private DateTime dateTime;
    private boolean isPrivate;
    private String token;

    public Memento(Repository repo, String key, String dateTime) {
        this(repo, key, DateTime.parse(dateTime, fmt));
    }

    public Memento(Repository repo, String key, String dateTime, boolean isPrivate, String token) {
        this(repo, key, DateTime.parse(dateTime, fmt), isPrivate, token);
    }

    public Memento(Repository repo, String key, DateTime dateTime) {
        this.repository = repo;
        this.key = key;
        this.dateTime = dateTime;
    }

    public Memento(Repository repo, String key, DateTime dateTime, boolean isPrivate, String token) {
        this.repository = repo;
        this.key = key;
        this.dateTime = dateTime;
        this.isPrivate = isPrivate;
        this.token = token;
    }

    public Graph resolve() throws HttpException, IOException, URISyntaxException {
        URI uri = new URI(getMementoUri().toString());

        if (isPrivate) {
            return resolvePrivate(uri.toASCIIString());
        } else {
            return RDFDataMgr.loadGraph(uri.toASCIIString());
        }
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

    private Graph resolvePrivate(String url) throws IOException {
        HttpGet request = new HttpGet(url);
        request.addHeader(HeaderConstants.AUTHORIZATION, "token " + token);
        request.addHeader("Accept", "application/n-triples");
        HttpClient httpClient = HttpClients.custom().build();

        try {
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            Graph g = GraphFactory.createDefaultGraph();
            RDFDataMgr.read(g, entity.getContent(), Lang.N3);
            return g;
        } catch (IOException e) {
            throw new IOException("Failed to resolve from private repo.", e);
        }
    }
}

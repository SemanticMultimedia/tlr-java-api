package de.hpi.rdf.tailrapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by magnus on 01.06.16.
 */
@Singleton
public class TailrClient {

    private static Logger L = LogManager.getLogger(TailrClient.class);

    private static TailrClient instance;

    private URI tailrUri;

    private String user;
    private String token;

    /**
     * Get the test instance.
     *
     * @return an instance to test the tailr system
     * @throws URISyntaxException if the provided base URI is not valid
     */
    protected static TailrClient getInstance() throws URISyntaxException {
        return getInstance("http://tailr.s16a.org/", "mgns", "");
    }

    /**
     * Gets an instance of the tailr client.
     *
     * @param tailrUri the base URI for tailr
     * @param user     the tailr user
     * @param token    the authentication token
     * @return a new instance or an old one
     * @throws URISyntaxException if the provided base URI is not valid
     */
    public static TailrClient getInstance(String tailrUri, String user, String token) throws URISyntaxException {
        if (instance == null) {
            try {
                instance = new TailrClient(tailrUri, user, token);
            } catch (URISyntaxException e) {
                L.error("Unable to parse tailr base URI " + tailrUri, e);
                throw new URISyntaxException(tailrUri, "Unale to parse tailr base URI " + e.getMessage());
            }
        }
        return instance;
    }

    private TailrClient(String tailrUri, String user, String token) throws URISyntaxException {
        this.tailrUri = new URI(tailrUri);
        this.user = user;
        this.token = token;
    }

    public URI getTailrUri() {
        return tailrUri;
    }

    public String getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    /**
     * Close the tailr client. Actually this only
     * removes the pointer to the client instance.
     */
    public void close() {
        instance = null;
    }

    public HttpGet getGet(String url) {
        HttpGet request = new HttpGet(url);
        return request;
    }

    public HttpGet getAuthGet(String url) {
        HttpGet request = new HttpGet(url);
        request.addHeader("Authorization", "token " + this.token);
        return request;
    }

    public HttpResponse getResponse(HttpUriRequest request) {
        HttpClient httpClient = HttpClients.custom().build();

        try {
            L.info("Requesting: " + request.getMethod() + " " + request.getURI());
            HttpResponse response = httpClient.execute(request);
            L.info("Response state: " + response.getStatusLine());

            return response;
        } catch (IOException e) {
            L.error("HTTP request failed. Reason: ", e);
            return null;
        }
    }

    public JsonNode getResponseAsJson(HttpUriRequest request) throws IOException {
        request.addHeader("Accept", "application/json");
        HttpResponse response = getResponse(request);
        HttpEntity entity = response.getEntity();

        String responseString = null;
        try {
            responseString = EntityUtils.toString(entity, "UTF-8");
            return (new ObjectMapper()).readTree(responseString);
        } catch (IOException e) {
            L.error("Failed reading JSON response.", e);
            throw new IOException("Failed reading JSON response.", e);
        }
    }

    public List<Repository> getUserRepositories() throws IOException {
        return getUserRepositories(this.user);
    }

    public List<Repository> getUserRepositories(String user) throws IOException {
        List<Repository> repos = new ArrayList<Repository>();

        HttpGet httpGet = getGet(tailrUri.toString() + "api/" + user);

        JsonNode jsonNode = getResponseAsJson(httpGet);

        for (JsonNode repoNode: jsonNode.get("repositories").get("list").findValues("")) {
            Repository repo = new Repository(user, repoNode.get("name").textValue());
            repos.add(repo);
        }

        return repos;
    }

    public List<String> getRepositoryKeys(Repository repository) {
        List<String> keys = new ArrayList<String>();

        for (int page = 1; ; page = page + 1) {
            HttpGet httpGet = getGet(tailrUri.toString() + "api/" + repository.getUser() + "/" + repository.getName() + "?index=true&page=" + page);
            HttpResponse response = getResponse(httpGet);

            HttpEntity entity = response.getEntity();

            String responseString = null;
            try {
                responseString = EntityUtils.toString(entity, "UTF-8");

                // termination
                if (responseString.length() == 0) {
                    break;
                }

                String[] keysplit = responseString.split("\n");

                for (String key : keysplit) {
                    keys.add(key);
                }
            } catch (IOException e) {
                L.error("Failed to get repository keys on page " + page, e);
                break;
            }
        }

        return keys;
    }

    public List<Memento> getMementos(Repository repo, String key) throws IOException {
        List<Memento> mementos = new ArrayList<Memento>();

        HttpGet httpGet = getGet(tailrUri.toString() + "api/" + repo.getUser() + "/" + repo.getName() + "?key=" + URLEncoder.encode(key, "UTF8") + "&timemap=true");

        JsonNode jsonNode = getResponseAsJson(httpGet);

        for (JsonNode mementoNode: jsonNode.get("mementos").get("list")) {
            Memento memento = new Memento(repo, key, mementoNode.get("datetime").textValue());
            mementos.add(memento);
        }

        return mementos;
    }

    /**
     * Gets last stored {@link Memento}. Actually a fake method generating
     * a memento uri with the current date. Since the tailr system goes back to
     * the last stored time point this is a valid method.
     *
     * @param repo the repository
     * @param key  the key
     * @return the latest stored memento
     */
    public Memento getLatestMemento(Repository repo, String key) {
        DateTime t = new DateTime();
        return new Memento(repo, key, t);
    }

    public StatusLine deleteMemento(Memento m) throws IOException {
        HttpDelete httpDel = null;
        try {
            httpDel = new HttpDelete(m.getMementoUri(tailrUri).toString() + "&update=true");
        } catch (UnsupportedEncodingException | URISyntaxException e) {
            L.error("Failed to create memento URI.", e);
            throw new IOException("Failed to create memento URI.", e);
        }

        httpDel.addHeader("Authorization", "token " + this.token);

        HttpResponse response = getResponse(httpDel);

        return response.getStatusLine();
    }

    /**
     * Retrieves a {@link Delta} for a given {@link Memento}.
     * The delta designates the difference between the given memento
     * and the one before.
     *
     * @param mem a given memento
     * @return the delta between the given and the one before
     * @throws IOException        the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    public Delta getDelta(Memento mem) throws IOException, URISyntaxException {
        Delta d = new Delta();

        HttpGet get = getGet(mem.getMementoUri(tailrUri) + "&delta=true");
        JsonNode jsonNode = getResponseAsJson(get);

        for (JsonNode node : jsonNode.get("added")) {
            d.getAddedTriples().add(node.textValue());
        }
        for (JsonNode node : jsonNode.get("deleted")) {
            d.getRemovedTriples().add(node.textValue());
        }
        return d;
    }

    /**
     * Gets the {@link Delta} for the latest stored {@link Memento} and the memento
     * before.
     *
     * @param repo the repository
     * @param key  the key
     * @return the latest delta
     * @throws IOException        the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    public Delta getLatestDelta(Repository repo, String key) throws IOException, URISyntaxException {
        Memento mem = getLatestMemento(repo, key);
        return getDelta(mem);
    }
}

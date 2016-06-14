package de.hpi.rdf.tailrapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

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

    private static TailrClient instance;

    private URI tailrUri;

    private String user;
    private String token;

    public static TailrClient getInstance() {
        if (instance == null) {
            try {
                instance = new TailrClient("http://tailr.s16a.org/", "mgns", "aa560cf17b994ddc640c6795fa54a20053aea8cd");
            } catch (URISyntaxException e) {
                e.printStackTrace();
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

    public void close() {

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
            System.out.println(request.getMethod() + " " + request.getURI());

            HttpResponse response = httpClient.execute(request);

            System.out.println(response.getStatusLine());

            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JsonNode getResponseAsJson(HttpUriRequest request) {
        request.addHeader("Accept", "application/json");
        HttpResponse response = getResponse(request);

        HttpEntity entity = response.getEntity();

        String responseString = null;
        try {
            responseString = EntityUtils.toString(entity, "UTF-8");
            JsonNode jsonNode = (new ObjectMapper()).readTree(responseString);
            return jsonNode;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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

    public List<String> getRepositoryKeys(Repository repository) throws IOException {
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
                e.printStackTrace();
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

    public StatusLine deleteMemento(Memento m) {
        HttpDelete httpDel = null;
        try {
            httpDel = new HttpDelete(m.getMementoUri().toString() + "&update=true");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        httpDel.addHeader("Authorization", "token " + this.token);

        HttpResponse response = getResponse(httpDel);

        return response.getStatusLine();
    }
}

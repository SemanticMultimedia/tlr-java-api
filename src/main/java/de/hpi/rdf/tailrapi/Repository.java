package de.hpi.rdf.tailrapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URL;
import java.util.List;

/**
 * Created by magnus on 01.06.16.
 */
public class Repository {

    private String user;
    private String name;
    private URL uri;
    private List<String> keys;

    public Repository(String user, String name) {
        this.user = user;
        this.name = name;

        initialize();
    }

    private void initialize() {
        // get key index

    }

    public List<String> getKeys() {
        return keys;
    }

    public String getUser() {
        return user;
    }

    public String getName() {
        return name;
    }
}

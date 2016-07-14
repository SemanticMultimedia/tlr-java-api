package de.hpi.rdf.tailrapi;

import org.apache.http.StatusLine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * This is a basic interface for the tailr client.
 * It is intended to be used for testing, more precisely
 * mocking the tailr client in other applications.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public interface Tailr {

    /**
     * Gets all user repositories.
     *
     * @return the list of user repositories
     * @throws IOException the io exception
     */
    public List<Repository> getUserRepositories() throws IOException;

    /**
     * Gets all user repositories for a other then the default user.
     *
     * @param user the user name
     * @return the list of user repositories
     * @throws IOException the io exception
     */
    public List<Repository> getUserRepositories(String user) throws IOException;

    /**
     * Gets keys belonging to a repository.
     *
     * @param repository the repository
     * @return the repository keys
     */
    public List<String> getRepositoryKeys(Repository repository);

    /**
     * Gets all mementos stored und one key.
     *
     * @param repo the repository
     * @param key  the key
     * @return the list of mementos
     * @throws IOException the io exception
     */
    public List<Memento> getMementos(Repository repo, String key) throws IOException;

    /**
     * Gets last stored {@link Memento}. Actually a fake method generating
     * a memento uri with the current date. Since the tailr system goes back to
     * the last stored time point this is a valid method.
     *
     * @param repo the repository
     * @param key  the key
     * @return the latest stored memento
     */
    public Memento getLatestMemento(Repository repo, String key);

    /**
     * Delete a specificmemento .
     *
     * @param m the memento for deletion
     * @return response status line
     * @throws IOException the io exception
     */
    public StatusLine deleteMemento(Memento m) throws IOException;

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
    public Delta getDelta(Memento mem) throws IOException, URISyntaxException;

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
    public Delta getLatestDelta(Repository repo, String key) throws IOException, URISyntaxException;

    /**
     * Creates a new {@link Memento} version and
     * stores the given content to tailr. The Memento is inserted
     * as last version and the corresponding delta is returned.
     *
     * @param repo    the memento repository
     * @param key     the memento key
     * @param content the storage content
     * @return the delta between the uploaded and previous version
     * @throws IOException        if the put fails
     * @throws URISyntaxException if the key can not be parsed
     */
    public Delta putMemento(Repository repo, String key, String content) throws IOException, URISyntaxException;

}

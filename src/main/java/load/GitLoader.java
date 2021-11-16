package load;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.pmw.tinylog.Logger;

import datasets.LoadingParameter;
import datasets.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Class for loading Gits from several sources.
 *
 * @author Soeren Viegener
 */
public class GitLoader {

    private static final String DEFAULT_REPOSITORIES_DIRECTORY = "repositories";

    public static Git loadRepository(Repository repo) {
    	Git git;
        if (repo.getRepoLocation() == LoadingParameter.FROM_DIR) {
            Logger.info("Loading git from {} ...", repo.getRepositoryPath());
            git = fromDefaultDirectory(repo.getRepositoryPath());
        } else if (repo.getRepoLocation() == LoadingParameter.FROM_ZIP) {
            Logger.info("Loading git from {} ...", repo.getRepositoryPath());
            git = fromZip(repo.getRepositoryPath());
        } else if (repo.getRepoLocation() == LoadingParameter.FROM_REMOTE) {
            Logger.info("Loading git from {} ...", repo.getRepositoryPath());
            git = fromRemote(repo.getRepositoryPath(), repo.getRepositoryName());
        } else {
            Logger.error("Failed to load");
            git = null;
        }
        return git;
    }
    
    /**
     * Loads a Git from a directory
     * @param dirname the name of the directory where the git repository is located
     * @return A Git object of the repository
     */
    private static Git fromDirectory(String dirname){
        try {
            return Git.open(new File(dirname));
        } catch (IOException e) {
            Logger.warn("Failed to load git repo from {}", dirname);
            return null;
        }
    }

    /**
     * Loads a Git from a directory located in the default repositories directory
     * @param dirname The name of the directory in the default repositories directory
     * @return A Git object of the repository
     */
    private static Git fromDefaultDirectory(String dirname){
        return fromDirectory(DEFAULT_REPOSITORIES_DIRECTORY + "/" + dirname);
    }

    /**
     * Loads a Git from a remote repository
     * @param remoteUri URI of the remote git repository
     * @param repositoryName Name of the repository. Sets the directory name in the default repositories directory where this repository is cloned to
     * @return A Git object of the repository
     */
    private static Git fromRemote(String remoteUri, String repositoryName){
        try {
            Git git = Git.cloneRepository()
                    .setURI( remoteUri )
                    .setDirectory(Paths.get(DEFAULT_REPOSITORIES_DIRECTORY, repositoryName).toFile())
                    .call();
            return git;
        } catch (GitAPIException e) {
            Logger.warn("Failed to load git repo from {}", remoteUri);
            return null;
        }
    }

    /**
     * Loads a Git from a zipped repository
     * @param zipFileName Name of the zip file located in the default repositories directory
     * @return A Git object of the repository
     */
    private static Git fromZip(String zipFileName){
        String pathname = DEFAULT_REPOSITORIES_DIRECTORY + "/" + zipFileName;
        try {
            ZipFile zipFile = new ZipFile(pathname);
            zipFile.extractAll(DEFAULT_REPOSITORIES_DIRECTORY);
        } catch (ZipException e) {
            Logger.warn("Failed to extract git repo from {}", pathname);
            return null;
        }
        return fromDefaultDirectory(zipFileName.substring(0, zipFileName.length()-4));
    }
}

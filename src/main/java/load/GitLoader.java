package load;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.pmw.tinylog.Logger;
import util.IO;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

/**
 * Class for loading Gits from several sources.
 *
 * @author Soeren Viegener, Paul Maximilian Bittner
 */
public class GitLoader {
    /**
     * Loads a Git from a directory
     * @param dirname the name of the directory where the git repository is located
     * @return A Git object of the repository
     */
    public static Git fromDirectory(Path dirname){
        try {
            return Git.open(dirname.toFile());
        } catch (IOException e) {
            Logger.warn("Failed to load git repo from {}", dirname);
            return null;
        }
    }

    /**
     * Loads a Git from a remote repository
     * @param localPath Directory where the remote repository is cloned to.
     * @param remoteURI URI of the remote git repository
     * @return A Git object of the repository
     */
    public static Git fromRemote(Path localPath, URI remoteURI){
        try {
            return Git.cloneRepository()
                    .setURI(remoteURI.toString())
                    .setDirectory(localPath.toFile())
                    .call();
        } catch (GitAPIException e) {
            Logger.warn("Failed to load git repo from {}", remoteURI);
            return null;
        }
    }

    /**
     * Loads a Git from a zipped repository
     * @param pathToZip Name of the zip file located in the default repositories directory
     * @return A Git object of the repository
     */
    public static Git fromZip(Path pathToZip) {
        final Path targetDir = pathToZip.getParent();
        try {
            ZipFile zipFile = new ZipFile(pathToZip.toFile());
            zipFile.extractAll(targetDir.toString());
        } catch (ZipException e) {
            Logger.warn("Failed to extract git repo from {} to {}", pathToZip, targetDir);
            return null;
        }

        return fromDirectory(targetDir.resolve(IO.withoutFileExtension(pathToZip.getFileName().toString())));
    }
}

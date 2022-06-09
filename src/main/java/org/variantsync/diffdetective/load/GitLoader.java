package org.variantsync.diffdetective.load;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.tinylog.Logger;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.FileUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class for loading Gits from several sources.
 *
 * @author Soeren Viegener, Paul Maximilian Bittner
 */
public class GitLoader {
    /**
     * Loads a Git from a directory
     * @param pathToRepo the name of the directory where the git repository is located
     * @return A Git object of the repository
     */
    public static Git fromDirectory(Path pathToRepo){
        Assert.assertTrue(Files.isDirectory(pathToRepo), "Given path " + pathToRepo + " is not a directory!");

        try {
            return Git.open(pathToRepo.toFile());
        } catch (IOException e) {
            Logger.warn("Failed to load git repo from {}", pathToRepo);
            return null;
        }
    }

    /**
     * Loads a Git from a remote repository
     * @param localPath Directory where the remote repository is cloned to.
     * @param remoteURI URI of the remote git repository
     * @return A Git object of the repository
     */
    public static Git fromRemote(Path localPath, URI remoteURI) {
        if (!Files.exists(localPath)) {
            Assert.assertTrue(localPath.toFile().mkdirs(), "Could not create directory for repository at " + localPath + "!");
        }

        Assert.assertTrue(Files.isDirectory(localPath), "Given path " + localPath + " is not a directory!");

        // If the repository is already cloned, use the clone.
        if (!FileUtils.tryIsEmptyDirectory(localPath)) {
            return fromDirectory(localPath);
        }

        try {
            Logger.info("Cloning {} to {}.", remoteURI, localPath);
            return Git
                    .cloneRepository()
                    .setURI(remoteURI.toString())
                    .setDirectory(localPath.toFile())
                    .setProgressMonitor(new LoggingProgressMonitor())
                    .call();
        } catch (GitAPIException e) {
            throw new RuntimeException("Failed to load git repo from " + remoteURI + " because:\n" + e);
        }
    }

    /**
     * Loads a Git from a zipped repository
     * @param pathToZip Name of the zip file located in the default repositories directory
     * @return A Git object of the repository
     */
    public static Git fromZip(Path pathToZip) {
        Assert.assertTrue(Files.isRegularFile(pathToZip), "Given path " + pathToZip + " is not a file!");

        final Path targetDir = pathToZip.getParent();
        final Path unzippedRepoName = Path.of(FilenameUtils.removeExtension(pathToZip.toString()));

        // If the repository is already unzipped, use the unzipped files.
        if (Files.exists(unzippedRepoName) && !FileUtils.tryIsEmptyDirectory(unzippedRepoName)) {
            return fromDirectory(unzippedRepoName);
        }

        try {
            ZipFile zipFile = new ZipFile(pathToZip.toFile());
            zipFile.extractAll(targetDir.toString());
        } catch (ZipException e) {
            Logger.warn("Failed to extract git repo from {} to {}", pathToZip, targetDir);
            return null;
        }

        return fromDirectory(unzippedRepoName);
    }
}

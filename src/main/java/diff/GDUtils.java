package diff;

import diff.data.GitDiff;

import java.io.*;

/**
 * Utility class for GitDiffs
 *
 * Deprecated because serializing a GitDiff does not work anymore.
 *
 * @author Sören Viegener
 */
@Deprecated
public class GDUtils {

    /**
     * Saves a serialized GitDiff to a file.
     *
     * @param gitDiff GitDiff to be serialized and saved.
     * @param file File to save to.
     * @throws IOException Thrown when saving fails
     */
    public static void saveGitDiff(GitDiff gitDiff, File file) throws IOException {
        FileOutputStream fileOutputStream
                = new FileOutputStream(file);
        ObjectOutputStream objectOutputStream
                = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(gitDiff);
        objectOutputStream.flush();
        objectOutputStream.close();
    }

    /**
     * Loads a serialized GitDiff from a file.
     *
     * @param file File to load from.
     * @return GitDiff loaded from file.
     * @throws IOException Thrown when loading the file fails
     * @throws ClassNotFoundException Thrown when the file does not contain a serialized GitDiff
     */
    public static GitDiff loadGitDiff(File file) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream
                = new FileInputStream(file);
        ObjectInputStream objectInputStream
                = new ObjectInputStream(fileInputStream);
        GitDiff gitDiff = (GitDiff) objectInputStream.readObject();
        objectInputStream.close();
        return gitDiff;
    }
}

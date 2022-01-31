

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Test;
import datasets.ParseOptions.DiffStoragePolicy;
import datasets.Repository;
import diff.CommitDiff;
import diff.GitDiffer;
import diff.PatchDiff;
import diff.result.CommitDiffResult;

/**
 * Perform "git diff" on a git repository.
 */
public class PrintWorkingTreeDiff {
	
	@Test
	public void testWorkingTreeDiff() {
		String repoName = "test_repo";
		
		// Retrieve repository
		final String repo_path = "repositories/" + repoName;
		final Repository repository = Repository.fromDirectory(Paths.get(repo_path), repoName);
		repository.setParseOptions(repository.getParseOptions().withDiffStoragePolicy(DiffStoragePolicy.REMEMBER_FULL_DIFF));
		
		// 
		try {
			final GitDiffer differ = new GitDiffer(repository);
			
			// Retrieve latest commit
			// Alternatively, replace with desired RevCommit
			RevCommit latestCommit = differ.getJGitRepo().log().setMaxCount(1).call().iterator().next(); 
			
			// Extract CommitDiff
			CommitDiffResult commitDiffResult = GitDiffer.createWorkingTreeDiff(differ.getJGitRepo(), repository.getDiffFilter(), latestCommit, repository.getParseOptions());
			CommitDiff commitDiff = commitDiffResult.unwrap().first().orElseThrow();
			
			// Save diff output
			String diffOutput = "";
			for (PatchDiff patchDiff : commitDiff.getPatchDiffs()) {
				diffOutput += patchDiff.getDiff();
				diffOutput = diffOutput.substring(0, diffOutput.length() - 1); // remove trailing white space
			}
			
			// Load diff to verfiy computed output
			String fileForVerification = System.getProperty("user.dir") + "/src/test/resources/" + repoName + ".txt";
			String result = read(fileForVerification);
			
			// Check whether diffs match
			Assert.assertTrue(diffOutput.equals(result));
			
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Read in diff from external file.
	 * 
	 * @param filePath Path to the file
	 * @return The diff
	 */
	private static String read(String filePath) {
		try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
	}
	
}

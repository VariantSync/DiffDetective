import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.variantsync.diffdetective.datasets.ParseOptions.DiffStoragePolicy;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.GitDiffer;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.result.CommitDiffResult;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Perform "git diff" on a git repository.
 */
@Disabled
public class PrintWorkingTreeDiff {
	
	@Test
	public void testWorkingTreeDiff() throws IOException, NoHeadException, GitAPIException {
		String repoName = "test_repo";
		
		// Retrieve repository
		final String repo_path = "repositories/" + repoName; 
		final Repository repository = Repository.fromZip(Paths.get(repo_path + ".zip"), repoName); // remove ".zip" when using fromDirectory()
		repository.setParseOptions(repository.getParseOptions().withDiffStoragePolicy(DiffStoragePolicy.REMEMBER_FULL_DIFF));
		
		final GitDiffer differ = new GitDiffer(repository);
		
		// Retrieve latest commit
		// Alternatively, replace with desired RevCommit
		RevCommit latestCommit = differ.getJGitRepo().log().setMaxCount(1).call().iterator().next(); 
		
		// Extract CommitDiff
		CommitDiffResult commitDiffResult = GitDiffer.createWorkingTreeDiff(differ.getJGitRepo(), repository.getDiffFilter(), latestCommit, repository.getParseOptions());
		CommitDiff commitDiff = commitDiffResult.diff().orElseThrow();
		
		// Save diff output
		String diffOutput = "";
		for (PatchDiff patchDiff : commitDiff.getPatchDiffs()) {
			diffOutput += patchDiff.getDiff();
		}
		
		// Check whether diffs match
		Path fileForVerification = Path.of("src", "test", "resources", repoName + ".txt");
		TestUtils.assertEqualToFile(fileForVerification, diffOutput);
		
	}
}

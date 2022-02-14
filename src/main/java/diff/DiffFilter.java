package diff;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A filter for commits and patches.
 * Can only be created using a DiffFilter.Builder.
 *
 * See field descriptions for more details.
 *
 * @author Sören Viegener
 */
public class DiffFilter {
    public static final DiffFilter ALLOW_ALL = new Builder()
            .allowMerge(true)
            .allowAllChangeTypes()
            .allowAllFileExtensions()
            .build();

    /**
     * A list of allowed file extensions for patches.
     * When this list is not empty all file extension that it does not contain will be filtered.
     */
    private final List<String> allowedFileExtensions;

    /**
     * A list of blocked file extensions for patches.
     * All file extensions in this list will be filtered.
     */
    private final List<String> blockedFileExtensions;

    /**
     * Allowed change types for patches.
     * When this list is not empty all change types that it does not contain will be filtered.
     */
    private final List<DiffEntry.ChangeType> allowedChangeTypes;

    /**
     * Regex of allowed file paths for patches.
     * When this is set, all file paths that do not match will be filtered.
     */
    private final List<String> allowedPaths;

    /**
     * Regex of blocked file paths for patches.
     * When this is set, all file paths that match will be filtered.
     */
    private final List<String> blockedPaths;

    /**
     * When set to true, all merge commits will be filtered.
     */
    private final boolean allowMerge;

    /**
     * Builder for a DiffFilter.
     *
     * See field descriptions of DiffFilter for more details.
     * @author Sören Viegener
     */
    public static class Builder {
        private final List<String> allowedFileExtensions;
        private final List<String> blockedFileExtensions;
        private final List<DiffEntry.ChangeType> allowedChangeTypes;
        private final List<String> allowedPaths;
        private final List<String> blockedPaths;
        private boolean allowMerge;

        public Builder() {
            allowedFileExtensions = new ArrayList<>();
            allowedChangeTypes = new ArrayList<>();
            blockedFileExtensions = new ArrayList<>();
            allowedPaths = new ArrayList<>();
            blockedPaths = new ArrayList<>();
            allowMerge = true;
        }

        public Builder(final DiffFilter other) {
            allowedFileExtensions = new ArrayList<>(other.allowedFileExtensions);
            allowedChangeTypes = new ArrayList<>(other.allowedChangeTypes);
            blockedFileExtensions = new ArrayList<>(other.blockedFileExtensions);
            allowedPaths = new ArrayList<>(other.allowedPaths);
            blockedPaths = new ArrayList<>(other.blockedPaths);
            allowMerge = other.allowMerge;
        }

        public Builder allowedPaths(String regex) {
            allowedPaths.add(regex);
            return this;
        }

        public Builder blockedPaths(String regex) {
            blockedPaths.add(regex);
            return this;
        }

        public Builder allowedFileExtensions(String... fileExtensions) {
            allowedFileExtensions.addAll(Arrays.asList(fileExtensions));
            return this;
        }

        public Builder blockedFileExtensions(String... fileExtensions) {
            blockedFileExtensions.addAll(Arrays.asList(fileExtensions));
            return this;
        }

        public Builder allowedChangeTypes(DiffEntry.ChangeType... changeTypes) {
            allowedChangeTypes.addAll(Arrays.asList(changeTypes));
            return this;
        }

        public Builder allowMerge(boolean allowMerge) {
            this.allowMerge = allowMerge;
            return this;
        }

        public Builder allowAllChangeTypes() {
            this.allowedChangeTypes.clear();
            return this;
        }

        public Builder allowAllFileExtensions() {
            this.allowedFileExtensions.clear();
            return this;
        }

        public Builder allowChangeType(DiffEntry.ChangeType changeType) {
            this.allowedChangeTypes.add(changeType);
            return this;
        }

        public Builder allowFileExtension(String fileExtension) {
            this.allowedFileExtensions.add(fileExtension);
            return this;
        }

        public DiffFilter build() {
            return new DiffFilter(this);
        }
    }

    private DiffFilter(Builder builder) {
        this.allowedFileExtensions = builder.allowedFileExtensions;
        this.allowedChangeTypes = builder.allowedChangeTypes;
        this.blockedFileExtensions = builder.blockedFileExtensions;
        this.allowedPaths = builder.allowedPaths;
        this.blockedPaths = builder.blockedPaths;
        this.allowMerge = builder.allowMerge;
    }

    public boolean filter(PatchDiff patchDiff) {
        if (!allowedPaths.isEmpty() && !isAllowedPath(patchDiff.getFileName())) {
            return false;
        }
        if (!blockedPaths.isEmpty() && isBlockedPath(patchDiff.getFileName())) {
            return false;
        }
        if (!allowedChangeTypes.isEmpty() && !allowedChangeTypes.contains(patchDiff.getChangeType())) {
            return false;
        }
        if (!allowedFileExtensions.isEmpty() && !allowedFileExtensions.contains(patchDiff.getFileExtension())) {
            return false;
        }
        if (!blockedFileExtensions.isEmpty() && blockedFileExtensions.contains(patchDiff.getFileExtension())) {
            return false;
        }
        return true;
    }

    public boolean filter(DiffEntry diffEntry) {
        if (!allowedPaths.isEmpty() &&
                !(isAllowedPath(diffEntry.getOldPath()) && isAllowedPath(diffEntry.getNewPath())))
        {
            return false;
        }
        if (!blockedPaths.isEmpty() &&
                (isBlockedPath(diffEntry.getOldPath()) || isBlockedPath(diffEntry.getNewPath())))
        {
            return false;
        }
        if (!allowedChangeTypes.isEmpty() &&
                !allowedChangeTypes.contains(diffEntry.getChangeType()))
        {
            return false;
        }
        if (!allowedFileExtensions.isEmpty() &&
                !(hasAllowedExtension(diffEntry.getOldPath()) && hasAllowedExtension(diffEntry.getNewPath())))
        {
            return false;
        }
        if (!blockedFileExtensions.isEmpty() &&
                (hasBlockedExtension(diffEntry.getOldPath()) || hasBlockedExtension(diffEntry.getNewPath())))
        {
            return false;
        }
        return true;
    }

    private boolean isAllowedPath(String filename) {
        return allowedPaths.stream().anyMatch(filename::matches);
    }

    private boolean isBlockedPath(String filename) {
        return blockedPaths.stream().anyMatch(filename::matches);
    }

    private boolean hasAllowedExtension(String filename) {
        return allowedFileExtensions.contains(getFileExtension(filename));
    }

    private boolean hasBlockedExtension(String filename) {
        return blockedFileExtensions.contains(getFileExtension(filename));
    }

    private String getFileExtension(String path){
        return FilenameUtils.getExtension(path).toLowerCase();
    }

    public boolean filter(RevCommit commit) {
        return this.allowMerge || commit.getParentCount() <= 1;
    }
}

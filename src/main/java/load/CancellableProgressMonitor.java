package load;

import org.eclipse.jgit.lib.ProgressMonitor;

public abstract class CancellableProgressMonitor implements ProgressMonitor {
    private boolean cancelled = false;

    void cancel() {
        cancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}

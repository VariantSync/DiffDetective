package org.variantsync.diffdetective.load;

import org.eclipse.jgit.lib.ProgressMonitor;
import org.tinylog.Logger;

/**
 * Logs the start of all tasks as {@code INFO} to the tinylog API.
 * This class will never cancel the monitored computation.
 */
public class LoggingProgressMonitor implements ProgressMonitor {
    @Override
    public void start(int totalTasks) {

    }

    @Override
    public void beginTask(String title, int totalWork) {
        Logger.info(title);
    }

    @Override
    public void update(int completed) {

    }

    @Override
    public void endTask() {

    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}

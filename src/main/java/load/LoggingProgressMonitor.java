package load;

import org.tinylog.Logger;

public class LoggingProgressMonitor extends CancellableProgressMonitor {
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
}

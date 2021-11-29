package load;

import org.pmw.tinylog.Logger;

public class LoggingProgressMonitor extends CancellableProgressMonitor {
    private float totalWork = 0;
    private float doneSoFar = 0;

    @Override
    public void start(int totalTasks) {
        this.totalWork = 0;
        doneSoFar = 0;
    }

    @Override
    public void beginTask(String title, int totalWork) {
        this.totalWork += totalWork;
        Logger.info(" === STARTING: ", title, " ===");
        Logger.info(totalWork);
    }

    @Override
    public void update(int completed) {
        doneSoFar += completed;
        Logger.info((doneSoFar / totalWork) + "%");
    }

    @Override
    public void endTask() {
        Logger.info(" === DONE ===");
    }
}

package shell;

import org.pmw.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * @author Alexander Schultheiß
 */
public class ShellExecutor {
    private final Consumer<String> outputReader;
    private final Consumer<String> errorReader;
    private final Path workDir;

    public ShellExecutor(Consumer<String> outputReader, Consumer<String> errorReader) {
        this.workDir = null;
        this.outputReader = outputReader;
        this.errorReader = errorReader;
    }

    public ShellExecutor(Consumer<String> outputReader, Consumer<String> errorReader, Path workDir) {
        this.workDir = workDir;
        this.outputReader = outputReader;
        this.errorReader = errorReader;
    }

    public List<String> execute(ShellCommand command) throws ShellException {
        return execute(command, this.workDir);
    }

    public List<String> execute(ShellCommand command, Path executionDir) throws ShellException {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            throw new SetupError("The synchronization study can only be executed under Linux!");
        }

        ProcessBuilder builder = new ProcessBuilder();
        if (executionDir != null) {
            builder.directory(executionDir.toFile());
        }
        Logger.debug("Executing '" + command + "' in directory " + builder.directory());
        builder.command(command.parts());

        Process process;
        ExecutorService outputFutureService;
        ExecutorService errorFutureService;
        Future<?> outputFuture;
        Future<?> errorFuture;
        List<String> output = new LinkedList<>();
        Consumer<String> shareOutput = s -> {
            output.add(s);
            outputReader.accept(s);
        };
        try {
            process = builder.start();

            outputFutureService = Executors.newSingleThreadExecutor();
            outputFuture = outputFutureService.submit(collectOutput(process.getInputStream(), shareOutput));

            errorFutureService = Executors.newSingleThreadExecutor();
            errorFuture = errorFutureService.submit(collectOutput(process.getErrorStream(), errorReader));
        } catch (IOException e) {
            Logger.error("Was not able to execute " + command, e);
            throw new ShellException(e);
        }

        int exitCode;
        try {
            exitCode = process.waitFor();
            outputFuture.get();
            errorFuture.get();
            outputFutureService.shutdown();
            errorFutureService.shutdown();
        } catch (InterruptedException | ExecutionException e) {
            Logger.error("Interrupted while waiting for process to end.", e);
            throw new ShellException(e);
        }

        return command.interpretResult(exitCode, output);
    }

    private Runnable collectOutput(final InputStream inputStream, final Consumer<String> consumer) {
        return () -> {
            try (inputStream; BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                reader.lines().forEach(consumer);
            } catch (IOException e) {
                Logger.error("Exception thrown while reading stream of Shell command.", e);
            }
        };
    }
}
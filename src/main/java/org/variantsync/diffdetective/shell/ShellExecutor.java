package org.variantsync.diffdetective.shell;

import org.tinylog.Logger;

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
 * This class can execute {@link ShellCommand}s in a specific working directory and redirect their
 * output to a Java function.
 *
 * @author Alexander Schultheiß
 */
public class ShellExecutor {
    private final Consumer<String> outputReader;
    private final Consumer<String> errorReader;
    private final Path workDir;

    /**
     * Constructs an executor of {@code ShellCommand}s in the current working directory.
     *
     * The current working directory is the project/Git root by default when started through maven
     * but this can be configured and is part of the contract between the caller of this function
     * and the caller of DiffDetective.
     *
     * @param outputReader will be provided the {@code stdout} of the executed command
     * @param errorReader will be provided the {@code stderr} of the executed command
     */
    public ShellExecutor(Consumer<String> outputReader, Consumer<String> errorReader) {
        this.workDir = null;
        this.outputReader = outputReader;
        this.errorReader = errorReader;
    }

    /**
     * Constructs an executor of {@code ShellCommand}s in the current working directory.
     *
     * @param outputReader will be provided the {@code stdout} of the executed command
     * @param errorReader will be provided the {@code stderr} of the executed command
     * @param workDir the default working directory for the executed commands
     */
    public ShellExecutor(Consumer<String> outputReader, Consumer<String> errorReader, Path workDir) {
        this.workDir = workDir;
        this.outputReader = outputReader;
        this.errorReader = errorReader;
    }

    /**
     * Execute {@code command} in the default working directory.
     *
     * the default working directory is the directory given in the
     * {@link ShellExecutor constructor} or the current working directory of this process of not
     * given.
     *
     * @param command the command to execute
     * @throws ShellException if the executable in command can't be executed or it exits with an
     * error exit code
     */
    public List<String> execute(ShellCommand command) throws ShellException {
        return execute(command, this.workDir);
    }

    /**
     * Execute {@code command} in the given working directory.
     *
     * The default working directory (given in the {@link ShellExecutor constructor}) isn't used.
     *
     * @param command the command to execute
     * @param executionDir the directory in which {@code command} is executed (working directory)
     * @throws ShellException if the executable in {@code command} can't be executed or it exits
     * with an error exit code
     */
    public List<String> execute(ShellCommand command, Path executionDir) throws ShellException {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            throw new SetupError("The synchronization study can only be executed under Linux!");
        }

        ProcessBuilder builder = new ProcessBuilder();
        if (executionDir != null) {
            builder.directory(executionDir.toFile());
        }
        Logger.debug("Executing '{}' in directory {}", command, builder.directory());
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
            Logger.error(e, "Was not able to execute {}", command);
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
            Logger.error(e, "Interrupted while waiting for process to end.");
            throw new ShellException(e);
        }

        return command.interpretResult(exitCode, output);
    }

    /** Feed the lines read from {@code inputStream} to {@code consumer}. */
    private Runnable collectOutput(final InputStream inputStream, final Consumer<String> consumer) {
        return () -> {
            try (inputStream; BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                reader.lines().forEach(consumer);
            } catch (IOException e) {
                Logger.error(e, "Exception thrown while reading stream of Shell command.");
            }
        };
    }
}

package org.variantsync.diffdetective.util;

import org.variantsync.functjonal.Lazy;

import java.text.NumberFormat;

/**
 * Human readable diagnostic information bundle.
 *
 * <p>This is a {@link INSTANCE singleton}.
 *
 * <p>Adapted from https://stackoverflow.com/a/8973770/5410757
 */
public class Diagnostics {
    public final static Lazy<Diagnostics> INSTANCE = Lazy.of(Diagnostics::new);
    private final Runtime runtime = Runtime.getRuntime();

    private Diagnostics() {}

    /** Human readable information about current memory usage and the running machine. */
    public String info() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.osInfo());
        sb.append(this.memInfo());
        return sb.toString();
    }

    public String osName() {
        return System.getProperty("os.name");
    }

    public String osVersion() {
        return System.getProperty("os.version");
    }

    /** Returns the architecture of the running system. */
    public String osArch() {
        return System.getProperty("os.arch");
    }

    public int getNumberOfAvailableProcessors() {
        return runtime.availableProcessors();
    }

    /** Convert Bytes into Gigabytes. */
    private static double B2GB(long bytes) {
        return bytes / 1048576.0; // = (1024.0 * 1024.0);
    }

    /** Human readable information about the current memory usage. */
    public String memInfo() {
        NumberFormat format = NumberFormat.getInstance();
        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        sb.append("Free memory: ");
        sb.append(format.format(B2GB(freeMemory)));
        sb.append("GB\n");
        sb.append("Allocated memory: ");
        sb.append(format.format(B2GB(allocatedMemory / 1024)));
        sb.append("GB\n");
        sb.append("Max memory: ");
        sb.append(format.format(B2GB(maxMemory / 1024)));
        sb.append("GB\n");
        sb.append("Total free memory: ");
        sb.append(format.format(B2GB(freeMemory + (maxMemory - allocatedMemory))));
        sb.append("GB\n");
        return sb.toString();
    }

    /** Human readable information about the running machine. */
    public String osInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("OS: ");
        sb.append(this.osName());
        sb.append("\n");
        sb.append("Version: ");
        sb.append(this.osVersion());
        sb.append("\n");
        sb.append("Architecture: ");
        sb.append(this.osArch());
        sb.append("\n");
        sb.append("Available processors (cores): ");
        sb.append(runtime.availableProcessors());
        sb.append("\n");
        return sb.toString();
    }
}

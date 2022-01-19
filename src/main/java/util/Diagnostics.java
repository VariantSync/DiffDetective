package util;

import java.text.NumberFormat;

/**
 * Adapted from https://stackoverflow.com/a/8973770/5410757
 */
public class Diagnostics {
    private final Runtime runtime = Runtime.getRuntime();

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

    public String osArch() {
        return System.getProperty("os.arch");
    }

    public int getNumberOfAvailableProcessors() {
        return runtime.availableProcessors();
    }

    private static double B2GB(long bytes) {
        return bytes / 1048576.0; // = (1024.0 * 1024.0);
    }

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

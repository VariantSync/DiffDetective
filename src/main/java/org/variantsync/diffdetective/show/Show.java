package org.variantsync.diffdetective.show;

import org.variantsync.diffdetective.show.diff.ShowGraphApp;
import org.variantsync.diffdetective.variation.diff.DiffTree;

public class Show {
    /**
     * Todos
     * - variation tree support
     * - node drag and drop
     * - edge tips
     * - menu for layout algo. selection
     * - menu for node format selection
     */
    
    public static void show(final DiffTree d) {
        int resx = 800;
        int resy = 600;

        new ShowGraphApp(d, resx, resy).run();
    }
}

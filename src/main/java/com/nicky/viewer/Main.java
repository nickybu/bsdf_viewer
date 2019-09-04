package com.nicky.viewer;

import com.nicky.engine.ViewerEngine;
import org.lwjgl.Version;

/**
 * <h1>Main</h1>
 * Main class used to run the viewer.
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class Main {

    public static void main(String[] args) {
        try {
            System.out.println("LWJGL " + Version.getVersion() + "..."); // Display version
            Viewer viewer = new Viewer(); // Initialise viewer
            ViewerEngine viewerEngine = new ViewerEngine("BRDF Visualiser", 1000, 700, viewer);
            viewerEngine.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}

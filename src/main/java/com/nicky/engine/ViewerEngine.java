package com.nicky.engine;

import com.nicky.BRDFManager;
import com.nicky.brdfs.BRDF;
import com.nicky.viewer.Viewer;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * <h1>Viewer Engine</h1>
 * Handles OpenGL management and interfaces with BRDF Framework
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class ViewerEngine implements Runnable {

    private final Window viewerWindow;
    private final InterfaceWindow interfaceWindow;
    private final Viewer viewer;
    private final MouseInput mouseInput;
    private final BRDFManager brdfManager;
    /**
     * Stores the Alias and BRDF instance of all registered BRDFs
     */
    private Map<String, BRDF> registeredBRDFs;

    public ViewerEngine(String windowTitle, int width, int height, Viewer viewer) throws Exception {
        // Setup BRDFs
        brdfManager = new BRDFManager();
        registeredBRDFs = new HashMap<>();

        viewerWindow = new Window(windowTitle, width, height);
        interfaceWindow = new InterfaceWindow("BRDF Properties", 400, height, viewer, this);
        mouseInput = new MouseInput();
        this.viewer = viewer;

    }

    @Override
    public void run() {
        try {
            init();
            renderLoop();

            // Free viewerWindow callbacks and destroyInterface viewerWindow
            interfaceWindow.cleanupInterface();

            viewerWindow.getKeyCallback().free();
            viewerWindow.getFramebufferSizeCallback().free();
            glfwFreeCallbacks(interfaceWindow.getWindow());

            glfwDestroyWindow(viewerWindow.getWindow());
            glfwDestroyWindow(interfaceWindow.getWindow());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            glfwTerminate();
            viewerWindow.getErrorCallback().free();
            cleanup();
        }
    }

    protected void init() throws Exception {
        registeredBRDFs = brdfManager.init();

        viewerWindow.init();
        changeCurrentContext("viewer");
        mouseInput.init(viewerWindow);
        viewer.init(viewerWindow, registeredBRDFs);
        interfaceWindow.init();

        positionWindows();
    }

    protected void renderLoop() {
        while (!viewerWindow.windowShouldClose()) {
            input();
            update();
            render();
        }
    }

    protected void input() {
        mouseInput.input(viewerWindow);
        viewer.input(viewerWindow, mouseInput);
    }

    protected void update() {
        viewer.update(mouseInput);
    }

    protected void render() {
        viewer.render(viewerWindow);
        viewerWindow.update();
        changeCurrentContext("interface");
        interfaceWindow.update();
        changeCurrentContext("viewer");
    }

    protected void cleanup() {
        viewer.cleanup();
    }

    protected void positionWindows() {
        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidthViewer = stack.mallocInt(1);
            IntBuffer pHeightViewer = stack.mallocInt(1);
            IntBuffer pWidthInterface = stack.mallocInt(1);
            IntBuffer pHeightInterface = stack.mallocInt(1);

            // Get the viewerWindow size passed to glfwCreateWindow
            glfwGetWindowSize(viewerWindow.getWindow(), pWidthViewer, pHeightViewer);

            // Get the interfaceWindow size passed to glfwCreateWindow
            glfwGetWindowSize(interfaceWindow.getWindow(), pWidthInterface, pHeightInterface);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            int xposViewer = (vidmode.width() - (pWidthViewer.get(0) + pWidthInterface.get(0))) / 2;
            int xposInterface = xposViewer + pWidthViewer.get(0);
            int ypos = (vidmode.height() - pHeightViewer.get(0)) / 2;

            // Place the viewerWindow
            glfwSetWindowPos(viewerWindow.getWindow(), xposViewer, ypos);
            glfwSetWindowPos(interfaceWindow.getWindow(), xposInterface, ypos);

        } // the stack frame is popped automatically
    }

    protected void changeCurrentContext(String window) {
        if (window.equals("interface")) {
            glfwMakeContextCurrent(interfaceWindow.getWindow());
        } else if (window.equals("viewer")) {
            glfwMakeContextCurrent(viewerWindow.getWindow());
        }
    }

    public List<String> updateBRDFList() {
        registeredBRDFs = brdfManager.init();
        return brdfManager.getBrdfNameList();
    }

    public List<String> getBRDFNames() {
        return brdfManager.getBrdfNameList();
    }

    public Map<String, BRDF> getRegisteredBRDFs() {
        return registeredBRDFs;
    }

    public InterfaceWindow getInterfaceWindow() {
        return interfaceWindow;
    }
}

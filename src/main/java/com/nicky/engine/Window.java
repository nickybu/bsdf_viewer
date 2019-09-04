package com.nicky.engine;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * <h1>Window</h1>
 * GLFW Window Management for Viewer Window
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class Window {

    private final String title;
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;
    private GLFWFramebufferSizeCallback framebufferSizeCallback;
    private long window;
    private int width;
    private int height;
    private boolean resized;
    private Map<Integer, Integer> oldKeyStates = new HashMap<>();
    private int oldKeyState = GLFW_RELEASE;

    public Window(String windowTitle, int width, int height) {
        this.title = windowTitle;
        this.width = width;
        this.height = height;
        this.resized = false;
    }

    public void init() {
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err)); //setup error callback

        // Initialise GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // window will be resizable

        // Anti-aliasing
        glfwWindowHint(GLFW_SAMPLES, 4);

        // Create window
        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Key callback
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true); //detecting in rendering loop
                }
            }
        });

        // Resize callback
        glfwSetFramebufferSizeCallback(window, framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    width = w;
                    height = h;
                }
            }
        });

        glfwMakeContextCurrent(window);

        // Enable v-sync to refresh rate of GPU
        glfwSwapInterval(1);

        // Make window visible
        glfwShowWindow(window);

        GL.createCapabilities();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    }

    public boolean windowShouldClose() {
        return glfwWindowShouldClose(window);
    }

    public void update() {
        glfwSwapBuffers(window);
//        glfwPollEvents();
    }

    public void setClearColor(float r, float g, float b, float alpha) {
        glClearColor(r, g, b, alpha);
    }

    // Getters & Setters
    public long getWindow() {
        return window;
    }

    public GLFWErrorCallback getErrorCallback() {
        return errorCallback;
    }

    public GLFWKeyCallback getKeyCallback() {
        return keyCallback;
    }

    public GLFWFramebufferSizeCallback getFramebufferSizeCallback() {
        return framebufferSizeCallback;
    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isResized() {
        return resized;
    }

    public void setResized(boolean resized) {
        this.resized = resized;
    }

    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(window, keyCode) == GLFW_PRESS;
    }

    public boolean isKeyPressedOnce(int keyCode) {
        // Find old key state
        int oldState = -1;
        if (oldKeyStates.containsKey(keyCode)) {
            oldState = oldKeyStates.get(keyCode);
        } else {
            oldKeyStates.put(keyCode, GLFW_RELEASE);
        }

        int newState = glfwGetKey(window, keyCode);
        if (newState == GLFW_RELEASE && oldState == GLFW_PRESS) {
            oldKeyStates.replace(keyCode, newState);
            return true;
        }
        oldKeyStates.replace(keyCode, newState);
        return false;
    }
}

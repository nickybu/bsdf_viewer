package com.nicky.engine;

import org.joml.Vector2d;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

/**
 * <h1>Mouse Input</h1>
 * Handles mouse input
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class MouseInput {

    private final Vector2d prevPos;
    private final Vector2d currPos;
    private final Vector2f displVec; // displacement for x and y coordinates
    private boolean inWindow = false;
    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;

    public MouseInput() {
        prevPos = new Vector2d(-1, -1);
        currPos = new Vector2d(0, 0);
        displVec = new Vector2f();
    }

    public void init(Window window) {
        // Callback invoked when mouse is moved
        glfwSetCursorPosCallback(window.getWindow(), (windowHandle, xPos, yPos) -> {
            currPos.x = xPos;
            currPos.y = yPos;
        });
        // Callback invoked when mouse enters window.
        glfwSetCursorEnterCallback(window.getWindow(), (windowHandle, entered) -> {
            inWindow = entered;
        });
        // Callback invoked when a mouse button is pressed
        glfwSetMouseButtonCallback(window.getWindow(), (windowHandle, button, action, mode) -> {
            leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
        });
    }

    public Vector2f getDisplVec() {
        return displVec;
    }

    // Calculates mouse displacement from previous position
    public void input(Window window) {
        displVec.x = 0;
        displVec.y = 0;

        if (prevPos.x > 0 && prevPos.y > 0 && inWindow) {
            double deltaX = currPos.x - prevPos.x;
            double deltaY = currPos.y - prevPos.y;
            boolean rotateX = deltaX != 0;
            boolean rotateY = deltaY != 0;

            if (rotateX) {
                displVec.y = (float) deltaX;
            }
            if (rotateY) {
                displVec.x = (float) deltaY;
            }
        }
        prevPos.x = currPos.x;
        prevPos.y = currPos.y;
    }

    public boolean isLeftButtonPressed() {
        return leftButtonPressed;
    }

    public boolean isRightButtonPressed() {
        return rightButtonPressed;
    }
}

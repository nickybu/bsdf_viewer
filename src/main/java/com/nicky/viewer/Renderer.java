package com.nicky.viewer;

import com.nicky.engine.*;
import com.nicky.resources.Utils;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;

/**
 * <h1>Renderer</h1>
 * Handles render logic.
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class Renderer {

    // Projection Matrix variables
    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.0f;
    private ShaderProgram shaderProgram;
    private Transformation transformation;

    public Renderer() {
        transformation = new Transformation();
    }

    public void init(Window window) throws Exception {
        // Create shaders
        shaderProgram = new ShaderProgram();
        Utils utils = new Utils();
        shaderProgram.createVertexShader(utils.loadResource("src/main/java/com/nicky/shaders/VertexShader.glsl"));
        shaderProgram.createFragmentShader(utils.loadResource("src/main/java/com/nicky/shaders/FragmentShader.glsl"));
        shaderProgram.link();

        // Create uniforms for world and projection matrices
        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("modelViewMatrix");
    }

    // Clears the window
    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    public void render(Window window, ViewerItem[] viewerItems, Camera camera) {
        clear();

        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        shaderProgram.bind();

        // Update and Set projectionMatrix uniform
        Matrix4f projectionMatrix = transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        // Update view matrix
        Matrix4f viewMatrix = transformation.getViewMatrix(camera);

        // Render each object
        for (ViewerItem viewerItem : viewerItems) {

            Mesh mesh = viewerItem.getMesh();

            // Set model view matrix for object
            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(viewerItem, viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);

            // Render the mesh for this viewer item
            mesh.render();


        }

        shaderProgram.unbind();
    }

    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
    }
}
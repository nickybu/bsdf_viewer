package com.nicky.engine;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

/**
 * <h1>Shader Program</h1>
 * Manages fragment and vertex shaders
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class ShaderProgram {

    private final int programID;
    private final Map<String, Integer> uniforms; // store uniform identifiers
    private int vertexShaderID;
    private int fragmentShaderID;

    // Creates new OpenGL program
    public ShaderProgram() throws Exception {
        programID = glCreateProgram();

        if (programID == 0) {
            throw new Exception("Could not create Shader");
        }

        uniforms = new HashMap<>();
    }

    public void createVertexShader(String shaderCode) throws Exception {
        vertexShaderID = createShader(shaderCode, GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) throws Exception {
        fragmentShaderID = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    protected int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderID = glCreateShader(shaderType);

        if (shaderID == 0) {
            throw new Exception("Error creating shader. Type: " + shaderType);
        }

        glShaderSource(shaderID, shaderCode);
        glCompileShader(shaderID);

        // Check for compilation errors
        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling shader code: " + glGetShaderInfoLog(shaderID, 1024));
        }

        glAttachShader(programID, shaderID);
        return shaderID;
    }

    public void link() throws Exception {
        glLinkProgram(programID);

        if (glGetProgrami(programID, GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking shader code: " + glGetProgramInfoLog(programID, 1024));
        }

        if (vertexShaderID != 0) {
            glDetachShader(programID, vertexShaderID);
        }
        if (fragmentShaderID != 0) {
            glDetachShader(programID, fragmentShaderID);
        }

        /* Used for debugging purposes.
         * Tries to validate if the shader is correct given the current OpenGL state
         * Validation may fail even if the shader is correct, due to the fact that the
         * current state is not complete enough to run the shader
         */
        glValidateProgram(programID);

        if (glGetProgrami(programID, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validation shader code: " + glGetProgramInfoLog(programID, 1024));
        }
    }

    public void bind() {
        glUseProgram(programID);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void createUniform(String uniformName) throws Exception {
        int uniformLocation = glGetUniformLocation(programID, uniformName);

        if (uniformLocation < 0) {
            throw new Exception("Could not find uniform: " + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    public void setUniform(String uniformName, Matrix4f value) {
        // Dump matrix into float buffer
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer floatBuffer = stack.mallocFloat(16);
            value.get(floatBuffer);
            glUniformMatrix4fv(uniforms.get(uniformName), false, floatBuffer);
        }
    }

    public void cleanup() {
        unbind();
        if (programID != 0) {
            glDeleteProgram(programID);
        }
    }
}

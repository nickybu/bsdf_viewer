package com.nicky.engine;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * <h1>Mesh</h1>
 * Given an array of positions & indices, creates VBO and VAO needed to load the model into graphics card.
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class Mesh {

    private final int vaoID;
    private final List<Integer> vboIDList; // list to store VBO IDs
    private final int vertexCount;

    public Mesh(float[] positions, int[] indices, float[] colours) {

        // Create buffer in off-heap memory so that it's accessible by OpenGL library
        FloatBuffer positionBuffer = null;
        IntBuffer indicesBuffer = null;
        FloatBuffer colourBuffer = null;

        try {
            vboIDList = new ArrayList<>();
            vertexCount = indices.length;

            // Create VAO and bind it
            vaoID = glGenVertexArrays();
            glBindVertexArray(vaoID);

            // Create Position VBO, bind it and put positions into it
            int posVboID = glGenBuffers();
            vboIDList.add(posVboID);
            positionBuffer = MemoryUtil.memAllocFloat(positions.length);
            // Reset position of the buffer to the 0 position with flip()
            positionBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, posVboID);
            glBufferData(GL_ARRAY_BUFFER, positionBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); //coordinates in location 0

            // Create Index VBO and put indices in it
            int indexVboID = glGenBuffers();
            vboIDList.add(indexVboID);
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexVboID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
            // Define structure of our data and store it in an attribute list in VAO
            /*
             * index: location where shader expects this data
             * size: number of components per vertex attribute
             * type: type of each component
             * normalised: if value should be normalised
             * stride: byte offset between consecutive generic vertex attributes
             * pointer: offset to the first component in the buffer
             */
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); //coordinates in location 0

            // Create Colour VBO and put colours in it
            int colourVboID = glGenBuffers();
            vboIDList.add(colourVboID);
            colourBuffer = MemoryUtil.memAllocFloat(colours.length);
            colourBuffer.put(colours).flip();
            glBindBuffer(GL_ARRAY_BUFFER, colourVboID);
            glBufferData(GL_ARRAY_BUFFER, colourBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0); //colour in location 1

            // Unbind VBO
            glBindBuffer(GL_ARRAY_BUFFER, 0);

            // Unbind VAO
            glBindVertexArray(0);
        } finally {
            // Free off-heap memory allocated by FloatBuffer & IntBuffer
            if (positionBuffer != null) {
                memFree(positionBuffer);
            }
            if (indicesBuffer != null) {
                memFree(indicesBuffer);
            }
            if (colourBuffer != null) {
                memFree(colourBuffer);
            }
        }
    }

    public int getVaoID() {
        return vaoID;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void render() {
        // Draw the mesh
        // Bind to the VAO
        glBindVertexArray(getVaoID());
        glEnableVertexAttribArray(0); //enable attr1: coordinates
        glEnableVertexAttribArray(1); //enable attr2: colour

        // Draw vertices
//        glDrawArrays(GL_TRIANGLES, 0, mesh.getVertexCount());
        /*
         * mode: primitive for rendering
         * count: number of elements to be rendered
         * type: type of value in indices data
         * indices: offset to apply to indices data to start rendering
         */
        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);

        // Restore state
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDisableVertexAttribArray(0);

        // Delete VBO & VAO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int vboID : vboIDList) {
            glDeleteBuffers(vboID);
        }

        glBindVertexArray(0);
        glDeleteBuffers(vaoID);
    }
}

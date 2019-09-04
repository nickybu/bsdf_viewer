package com.nicky.engine;

import org.joml.Vector3f;

/**
 * <h1>Viewer Item</h1>
 * Encapsulates any object to be rendered in the scene.
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class ViewerItem {

    private final Mesh mesh;
    private final Vector3f position;
    private final Vector3f rotation;
    private float scale;

    public ViewerItem(Mesh mesh) {
        this.mesh = mesh;
        position = new Vector3f(0, 0, 0);
        scale = 1;
        rotation = new Vector3f(0, 0, 0);
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public void setRotation(float x, float y, float z) {
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;
    }
}

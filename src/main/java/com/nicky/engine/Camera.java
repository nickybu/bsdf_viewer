package com.nicky.engine;

import org.joml.Vector3f;

/**
 * <h1>Camera</h1>
 * Represents the state of the camera's position and rotation
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class Camera {

    private final Vector3f position;
    private final Vector3f rotation;

    public Camera() {
        position = new Vector3f(0, 0, 2);
        rotation = new Vector3f(0, 0, 0);
    }

    public Camera(Vector3f position, Vector3f rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
    }

    public void movePosition(float offsetX, float offsetY, float offsetZ) {
        if (offsetZ != 0) {
            position.x += (float) Math.sin(Math.toRadians(rotation.y)) * -1.0f * offsetZ;
            position.z += (float) Math.cos(Math.toRadians(rotation.y)) * offsetZ;

        }

        if (offsetX != 0) {
            position.x += (float) Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * offsetX;
            position.z += (float) Math.cos(Math.toRadians(rotation.y - 90)) * offsetX;
        }

        position.y += offsetY;

        if (rotation.x == 90) {
            if (position.y > 2.5f) {
                position.y = 2.5f;
            } else if (position.y < 0.1f) {
                position.y = 0.1f;
            }
        } else {
            if (position.z > 2.5f) {
                position.z = 2.5f;
            } else if (position.z < 0.1f) {
                position.z = 0.1f;
            }
        }
    }

    public void setRotation(float x, float y, float z) {
        rotation.x = x;
        rotation.y = y;
        rotation.z = z;
    }

    public void moveRotation(float offsetX, float offsetY, float offsetZ) {
        rotation.x += offsetX;
        rotation.y += offsetY;
//        rotation.z += offsetZ;
    }

    public void zoom(float offsetZ) {

        if (rotation.x == 90) {
            position.y += (float) Math.cos(Math.toRadians(rotation.y)) * offsetZ;
            if (position.y > 2.5f) {
                position.y = 2.5f;
            } else if (position.y < 0.1f) {
                position.y = 0.1f;
            }
        } else {
            position.z += (float) Math.cos(Math.toRadians(rotation.y)) * offsetZ;
            if (position.z > 2.5f) {
                position.z = 2.5f;
            } else if (position.z < 0.1f) {
                position.z = 0.1f;
            }
        }
    }
}

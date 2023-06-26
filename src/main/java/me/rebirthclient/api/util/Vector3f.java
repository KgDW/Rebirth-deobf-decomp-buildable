package me.rebirthclient.api.util;

import java.util.Objects;

public class Vector3f {
    public final float x;
    public final float y;
    public final float z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Vector3f vector3f = (Vector3f)o;
        return Float.compare(vector3f.x, this.x) == 0 && Float.compare(vector3f.y, this.y) == 0 && Float.compare(vector3f.z, this.z) == 0;
    }

    public int hashCode() {
        return Objects.hash(this.x, this.y, this.z);
    }
}


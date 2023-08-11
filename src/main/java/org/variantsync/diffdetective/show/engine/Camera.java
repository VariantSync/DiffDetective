package org.variantsync.diffdetective.show.engine;

import org.variantsync.diffdetective.show.engine.geom.Vec2;

public class Camera {
    private final static double zoomMin = 0.5, zoomMax = 50;
    private Vec2 location = Vec2.all(0);
    private double zoom = 1;

    /**
     * The camera zooms the given steps. If steps > 0 the camera will zoom in, else out.
     * x and y are the point, the camera should zoom at.
     */
    public void zoomTowards(int steps, Vec2 pos) {
        double zoomPrev = zoom;
        double newZoomSqrt = 0.2*steps + Math.sqrt(zoom);
        zoom = Math.min(zoomMax, Math.max(zoomMin, newZoomSqrt * newZoomSqrt));

        double dZoom = zoom - zoomPrev;
        this.location = this.location.minus(pos.scale(dZoom));
    }

    public void setZoom(double zoom) {
        if (zoom == 0)
            throw new IllegalArgumentException("Zoom can't be zero!");
        this.zoom = zoom;
    }

    public void setLocation(final Vec2 location) {
        this.location = location;
    }

    public double getZoom() { return zoom; }

    public Vec2 getLocation() {
        return location;
    }
}

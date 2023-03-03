package org.variantsync.diffdetective.show.engine;

public class Camera {
    private final static double zoomMin = 0.5, zoomMax = 50;

    private double x, y;
    private double zoom = 1;

    /**
     * The camera zooms the given steps. If steps > 0 the camera will zoom in, else out.
     * x and y are the point, the camera should zoom at.
     */
    public void zoom(int steps, double x, double y) {
        double zoomPrev = zoom;
        double newZoomSqrt = 0.2*steps + Math.sqrt(zoom);
        zoom = Math.min(zoomMax, Math.max(zoomMin, newZoomSqrt * newZoomSqrt));

        double dZoom = zoom - zoomPrev;
        this.x -= dZoom * x;
        this.y -= dZoom * y;
    }

    public void setZoom(double zoom) {
        if (zoom == 0)
            throw new IllegalArgumentException("Zoom can't be zero!");
        this.zoom = zoom;
    }

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }

    public double getY() { return y; }

    public double getZoom() { return zoom; }
}

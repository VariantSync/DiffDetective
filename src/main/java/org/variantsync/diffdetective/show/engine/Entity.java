package org.variantsync.diffdetective.show.engine;

import org.variantsync.diffdetective.show.engine.geom.Vec2;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.functjonal.Cast;

import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class Entity {
    private Vec2 location;
    private double z;
    private final AffineTransform relativeTransform;
    private final Map<Class<?>, EntityComponent> components;

    public Entity() {
        z = 0;
        location = Vec2.all(0);
        relativeTransform = new AffineTransform();
        this.components = new HashMap<>();
    }

    public void add(EntityComponent component) {
        Assert.assertNull(component.getEntity());

        Class<?> cls = component.getClass();
        do {
            final EntityComponent old = components.put(cls, component);
            if (old != null) {
                throw new RuntimeException("This entity has already a component of type " + cls.getSimpleName() + " registered!");
            }
            cls = cls.getSuperclass();
        } while (cls != null && !cls.equals(EntityComponent.class));

        component.setEntity(this);
    }

    public void remove(EntityComponent component) {
        Assert.assertTrue(component.getEntity() == this);

        Class<?> cls = component.getClass();
        do {
            components.remove(cls, component);
            cls = cls.getSuperclass();
        } while (cls != null && !cls.equals(EntityComponent.class));

        component.setEntity(null);
    }

    public <T extends EntityComponent> T get(final Class<T> type) {
        return Cast.unchecked(components.get(type));
    }

    public <T extends EntityComponent> void forComponent(
            final Class<T> type,
            Consumer<T> ifPresent
    ) {
        final T component = get(type);
        if (component != null) {
            ifPresent.accept(component);
        }
    }

    public AffineTransform getRelativeTransform() {
        return relativeTransform;
    }

    public void updateTransform() {
        relativeTransform.setTransform(
                1, 0,
                0, 1,
                location.x(), location.y());
    }

    public void setLocation(final Vec2 location) {
        Assert.assertNotNull(location);
        this.location = location;
        updateTransform();
    }

    public Vec2 getLocation() {
        return location;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getZ() {
        return z;
    }
}

package org.variantsync.diffdetective.show.engine;

@FunctionalInterface
public interface Updateable {
    void update(double deltaSeconds);
}

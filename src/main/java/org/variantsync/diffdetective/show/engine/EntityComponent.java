package org.variantsync.diffdetective.show.engine;

public abstract class EntityComponent {
    private Entity entity;

    protected void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}

package com.pseuco.np21.shared;

/**
 * Abstract representation for everything simulation related.
 * <p>
 * Ensures that every object has a unique id.
 */
public abstract class Entity implements Comparable<Entity> {
    /**
     * Counter for entities.
     */
    private static class EntityCounter {

        private static int counter;

        /**
         * Get the next entity number.
         *
         * @return next entity number
         */
        private static int next() {
            return counter++;
        }

    }

    /**
     * ID of the entity.
     */
    protected final int id = EntityCounter.next();

    /**
     * Constructs a new entity.
     */
    protected Entity() {
    }

    /**
     * Get the id of the entity.
     *
     * @return id of the entity
     */
    public int id() {
        return id;
    }

    @Override
    public int compareTo(Entity o) {
        return Integer.compare(id, o.id);
    }
}

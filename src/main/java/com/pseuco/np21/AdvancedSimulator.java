package com.pseuco.np21;

import com.pseuco.np21.shared.Recorder;
import com.pseuco.np21.shared.World;

/**
 * Advanced simulation manager.
 * <p>
 * Enables support for the second exercise.
 */
public class AdvancedSimulator extends Simulator {
    /**
     * {@code true} iff the second exercise was implemented.
     * <p>
     * TODO: change this if you want to submit the second exercise.
     */
    public static final boolean IS_IMPLEMENTED = false;

    private final World<Clearing, Trail> world;
    private final Recorder recorder;
    private final boolean modernBehavior;

    /**
     * Constructs a new advanced simulator.
     * <p>
     * You may change this except for the signature.
     *
     * @param world          to simulate
     * @param recorder       to record actions against
     * @param modernBehavior {@code true} iff the modern ant behavior shall be used during the simulation
     */
    public AdvancedSimulator(final World<Clearing, Trail> world, final Recorder recorder, final boolean modernBehavior) {
        super(world, recorder);
        this.world = world;
        this.recorder = recorder;
        this.modernBehavior = modernBehavior;
    }

    @Override
    public void run() {
        if (!modernBehavior) {
            super.run();
            return;
        }

        // TODO: implement the modern ant behavior here
        throw new UnsupportedOperationException("Not implemented");
    }
}

package com.alyxferrari.jse3d.obj;
import com.alyxferrari.jse3d.exc.*;
import com.alyxferrari.jse3d.gfx.*;
import com.alyxferrari.jse3d.interfaces.*;
import com.alyxferrari.jse3d.obj.update.*;
/** Represents a Particle's trajectory.
 * @author Alyx Ferrari
 * @since 2.5
 */
public class Trajectory {
	protected Script move;
	protected Particle particle = null;
	/** Creates a blank Trajectory with no actions.
	 */
	public Trajectory() {
		move = new Script() {
			@Override
			public void start() {}
			@Override
			public void update() {}
			@Override
			public void fixedUpdate() {}
			@Override
			public void stop() {}
		};
	}
	/** Creates a Trajectory with the specified script.
	 * @param script A MonoBehaviour-like script used to control the Particle attached to this Trajectory.
	 */
	public Trajectory(Script script) {
		this.move = script;
	}
	/** Gives this Particle linear movement given a direction and a Display object's Time variable. A Particle must have already been constructed with this Trajectory. (Display.getTime())
	 * @param increment How much this Particle should move every second. This vector is treated as a direction, not a point.
	 * @param time The Time variable of the Display object being used to render this Trajectory's particle.
	 * @return The Trajectory on which this method was called.
	 */
	public Trajectory setLinear(Vector3 increment, Time time) {
		if (particle == null) {
			throw new ParticleNotSetException();
		}
		move = new LinearUpdate(particle, increment, time);
		return this;
	}
	/** Runs the start() method of this Trajectory's script. Execute this only if you know what you're doing.
	 */
	public void start() {
		move.start();
	}
	/** Runs the update() method of this Trajectory's script. Execute this only if you know what you're doing.
	 */
	public void update() {
		move.update();
	}
	/** Runs the fixedUpdate() method of this Trajectory's script. Execute this only if you know what you're doing.
	 */
	public void fixedUpdate() {
		move.fixedUpdate();
	}
	/** Runs the stop() method of this Trajectory's script. Execute this only if you know what you're doing.
	 * @since 2.6.2
	 */
	public void stop() {
		move.stop();
	}
	void setParticle(Particle particle) {
		this.particle = particle;
	}
	/** Sets this Trajectory's script.
	 * @param script This Trajectory's new script.
	 * @return The Trajectory on which this method was called.
	 */
	public Trajectory setScript(Script script) {
		move = script;
		return this;
	}
}
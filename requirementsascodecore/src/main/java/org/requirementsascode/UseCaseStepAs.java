package org.requirementsascode;

import static org.requirementsascode.SystemReaction.continueAfterStep;
import static org.requirementsascode.SystemReaction.continueAtStep;
import static org.requirementsascode.SystemReaction.continueExclusivelyAtStep;

import java.util.Objects;
import java.util.function.Supplier;

import org.requirementsascode.exception.NoSuchElementInUseCase;


/**
 * The part of the step that contains a reference to the actors
 * that are allowed to trigger a system reaction for this step.
 * 
 * @author b_muth
 *
 **/
public class UseCaseStepAs{
	private Actor[] actors;
	private UseCaseStep useCaseStep;
	
	UseCaseStepAs(UseCaseStep useCaseStep, Actor... actor) {
		this.useCaseStep = useCaseStep;
		this.actors = actor;
		connectActorsToThisStep(actors);		
	}

	private void connectActorsToThisStep(Actor[] actors) {
		for (Actor actor : actors) {
			actor.newStep(useCaseStep);
		}
	}
	
	/**
	 * Defines the class of user event objects that this step accepts,
	 * so that they can cause a system reaction when the step's predicate is true.
	 * 
	 * Given that the step's predicate is true, the system reacts to objects that are
	 * instances of the specified class or instances of any direct or indirect subclass
	 * of the specified class.
	 * 
	 * Note: you must provide the event objects at runtime by calling {@link UseCaseRunner#reactTo(Object)}
	 * 
	 * @param eventClass the class of events the system reacts to in this step
	 * @param <T> the type of the class
	 * @return the created event part of this step
	 */
	public <T> UseCaseStepUser<T> user(Class<T> eventClass) {
		UseCaseStepUser<T> user = new UseCaseStepUser<>(useCaseStep, eventClass);
		useCaseStep.setUser(user);
		return user;
	}

	/**
	 * Without triggering a system reaction, raise the specified event.
	 * You may call this method several times during model creation, to raise
	 * several events.
	 * 
	 * Internally calls {@link UseCaseRunner#reactTo(Object)} for the specified event.
	 * 
	 * @param <U> the type of event to be raised
	 * @param eventSupplier the supplier proving the event
	 * @return the system part
	 */
	public <U> UseCaseStepSystem<SystemEvent> raise(Supplier<U> eventSupplier) {
		Objects.requireNonNull(eventSupplier);
		
		UseCaseStepSystem<SystemEvent> systemPartWithRaisedEvent 
			= system(() -> {}).raise(eventSupplier);
		return systemPartWithRaisedEvent;
	}
	
	/**
	 * Makes the use case runner continue at the specified step.
	 * If there are alternatives to the specified step, one may be entered
	 * if its condition is enabled.
	 * 
	 * @param stepName name of the step to continue at, in this use case.
	 * @return the use case this step belongs to, to ease creation of further flows
	 * @throws NoSuchElementInUseCase if no step with the specified stepName is found in the current use case
	 */
	public UseCase continueAt(String stepName) {
		Objects.requireNonNull(stepName);
		
		system(continueAtStep(useCaseStep.useCase(), stepName));
		return useCaseStep.useCase();
	}
	
	/**
	 * Makes the use case runner continue at the specified step. No alternative
	 * flow starting at the specified step is entered, even if its condition is
	 * enabled.
	 * 
	 * Note: the runner continues at the step only if its predicate is true, and
	 * actor is right.
	 * 
	 * @param stepName
	 *            name of the step to continue at, in this use case.
	 * @return the use case this step belongs to, to ease creation of further
	 *         flows
	 * @throws NoSuchElementInUseCase
	 *             if no step with the specified stepName is found in the
	 *             current use case
	 */
	public UseCase continueExclusivelyAt(String stepName) {
		Objects.requireNonNull(stepName);

		system(continueExclusivelyAtStep(useCaseStep.useCase(), stepName));
		return useCaseStep.useCase();
	}
	
	/**
	 * Makes the use case runner continue after the specified step.
	 * 
	 * @param stepName name of the step to continue after, in this use case.
	 * @return the use case this step belongs to, to ease creation of further flows
	 * @throws NoSuchElementInUseCase if no step with the specified stepName is found in the current use case
	 */
	public UseCase continueAfter(String stepName) {
		Objects.requireNonNull(stepName);
		
		system(continueAfterStep(useCaseStep.useCase(), stepName));
		return useCaseStep.useCase();
	}
	
	/**
	 * Defines an "autonomous system reaction",
	 * meaning the system will react when the step's predicate is true, without
	 * needing an event provided to the use case runner.
	 * 
	 * Instead of the model creator defining the event (via
	 * {@link #user(Class)}), the step implicitly handles the default
	 * system event. Default system events are raised by the 
	 * use case runner itself, causing "autonomous system reactions".
	 * 
	 * @param systemReaction the autonomous system reaction
	 * @return the created system part of this step
	 */
	public UseCaseStepSystem<SystemEvent> system(Runnable systemReaction) {
		UseCaseStepSystem<SystemEvent> system = 
			user(SystemEvent.class).system(se -> systemReaction.run());
		return system;
	}
	
	/**
	 * Returns the actors that determine which user groups can 
	 * cause the system to react to the event of this step.  
	 * 
	 * @return the actors
	 */
	public Actor[] actors() {
		return actors;
	}
}
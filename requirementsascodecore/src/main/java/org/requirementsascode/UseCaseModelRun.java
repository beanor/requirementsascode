package org.requirementsascode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.requirementsascode.UseCaseStep.ActorPart;
import org.requirementsascode.event.AutonomousSystemReactionEvent;
import org.requirementsascode.exception.MissingUseCaseStepPartException;
import org.requirementsascode.exception.MoreThanOneStepCouldReactException;

public class UseCaseModelRun {
	private List<Actor> actorsRunWith;
	private UseCaseModel useCaseModel;
	private UseCaseStep latestStep;
	private UseCaseFlow latestFlow;

	public UseCaseModelRun() {
		this.actorsRunWith = new ArrayList<>();
		this.useCaseModel = new UseCaseModel(this);
	}
	
	public UseCaseModel getModel() {
		return useCaseModel;
	}

	public UseCaseModelRun as(Actor actor) {
		Objects.requireNonNull(actor);
		
		Actor autonomousSystemActor = useCaseModel.getAutonomousSystemReactionActor();		
		actorsRunWith = Arrays.asList(actor, autonomousSystemActor);	
		
		triggerAutonomousSystemReaction();
		
		return this;
	}
	void triggerAutonomousSystemReaction() {
		reactTo(new AutonomousSystemReactionEvent());
	}
	
	public void reactTo(Object... events) {
		Objects.requireNonNull(events);
		for (Object event : events) {
			reactTo(event);
		}		
	}

	public <T> UseCaseStep reactTo(T event) {
		Objects.requireNonNull(event);
				
		Class<? extends Object> currentEventClass = event.getClass();
		Set<UseCaseStep> reactingUseCaseSteps = getEnabledSteps(currentEventClass);
		 
		UseCaseStep latestStepRun = triggerSystemReaction(event, reactingUseCaseSteps);
		
		return latestStepRun;
	}

	public Set<UseCaseStep> getEnabledSteps(Class<? extends Object> currentEventClass) {
		Stream<UseCaseStep> stepStream = useCaseModel.getUseCaseSteps().stream();
		Set<UseCaseStep> enabledSteps = getEnabledStepSubset(currentEventClass, stepStream);
		return enabledSteps;
	}
	
	private Set<UseCaseStep> getEnabledStepSubset(Class<? extends Object> currentEventClass, Stream<UseCaseStep> stepStream) {
		Set<UseCaseStep> enabledSteps = stepStream
			.filter(step -> stepActorIsRunActor(step))
			.filter(step -> stepEventClassIsSameOrSuperclassAsEventClass(step, currentEventClass))
			.filter(step -> isConditionFulfilled(step))
			.collect(Collectors.toSet());
		return enabledSteps;
	}

	private <T> UseCaseStep triggerSystemReaction(T event, Collection<UseCaseStep> useCaseSteps) {
		UseCaseStep useCaseStep = null;

		if(useCaseSteps.size() == 1){
			useCaseStep = useCaseSteps.iterator().next();
			triggerSystemReactionAndHandleException(event, useCaseStep);
		} else if(useCaseSteps.size() > 1){
			String message = getMoreThanOneStepCouldReactExceptionMessage(useCaseSteps);
			throw new MoreThanOneStepCouldReactException(message);
		}
		
		return useCaseStep;
	}
	
	private <T> UseCaseStep triggerSystemReactionAndHandleException(T event, UseCaseStep useCaseStep) {
		if(useCaseStep.getSystemPart() == null){
			String message = "Use Case Step \"" + useCaseStep + "\" has no defined system part! Please have a look and update your Use Case Model for this step!";
			throw new MissingUseCaseStepPartException(message);
		}
		
		setLatestStep(useCaseStep);
		setLatestFlow(useCaseStep.getUseCaseFlow());
		
		try {
			@SuppressWarnings("unchecked")
			Consumer<T> systemReaction = 
				(Consumer<T>) useCaseStep.getSystemPart().getSystemReaction();
			triggerSystemReaction(event, systemReaction);
		} 
		catch (Exception e) { 
			handleException(e);
		} 
		
		triggerAutonomousSystemReaction();

		return useCaseStep;
	}
	
	private <T> void triggerSystemReaction(T event, Consumer<T> systemReaction) {
		systemReaction.accept(event);
	}

	private void handleException(Exception e) {
		reactTo(e);
	}

	private <T> String getMoreThanOneStepCouldReactExceptionMessage(Collection<UseCaseStep> useCaseSteps) {
		String message = "System could react to more than one step: ";
		String useCaseStepsClassNames = useCaseSteps.stream().map(useCaseStep -> useCaseStep.toString())
				.collect(Collectors.joining(","));
		return message + useCaseStepsClassNames;
	}

	private boolean stepActorIsRunActor(UseCaseStep useCaseStep) {
		ActorPart<?> actorPart = useCaseStep.getActorPart();
		if(actorPart == null){
			String message = "Use Case Step \"" + useCaseStep + "\" has no defined actor part! Please have a look and update your Use Case Model for this step!";
			throw(new MissingUseCaseStepPartException(message));
		}
		
		return actorsRunWith.contains(actorPart.getActor());
	}
	
	private boolean stepEventClassIsSameOrSuperclassAsEventClass(UseCaseStep useCaseStep, Class<?> currentEventClass) {
		Class<?> stepEventClass = useCaseStep.getActorPart().getEventClass();
		return stepEventClass.isAssignableFrom(currentEventClass);
	}
	
	public UseCaseStep getLatestStep() {
		return latestStep;
	}
	
	public void setLatestStep(UseCaseStep latestStep) {
		this.latestStep = latestStep;
	}
	
	public UseCaseFlow getLatestFlow() {
		return latestFlow;
	}
	
	public void setLatestFlow(UseCaseFlow latestFlow) {
		this.latestFlow = latestFlow;
	}
	
	private boolean isConditionFulfilled(UseCaseStep useCaseStep) {
		Predicate<UseCaseModelRun> predicate = useCaseStep.getPredicate();
		
		if(predicate == null){
			predicate = 
				afterPreviousStepWhenNoOtherStepIsEnabled(useCaseStep);
		}	

		boolean result = predicate.test(this);
		return result;
	}
	
	private Predicate<UseCaseModelRun> afterPreviousStepWhenNoOtherStepIsEnabled(UseCaseStep useCaseStep) {
		return afterPreviousStep(useCaseStep).and(noOtherStepIsEnabled(useCaseStep));
	}

	private Predicate<UseCaseModelRun> afterPreviousStep(UseCaseStep thisStep) {
		return run -> {
			UseCaseStep latestStep = run.getLatestStep();
			return Objects.equals(thisStep.getPreviousStep(),latestStep);
		};
	}
	
	private Predicate<UseCaseModelRun> noOtherStepIsEnabled(UseCaseStep thisStep) {
		return run -> {
			Class<?> currentEventClass = thisStep.getActorPart().getEventClass();
			
			Stream<UseCaseStep> otherStepsStream = 
				useCaseModel.getUseCaseSteps().stream()
					.filter(step -> !step.equals(thisStep));
			
			Set<UseCaseStep> enabledOtherSteps = getEnabledStepSubset(currentEventClass, otherStepsStream);
			return enabledOtherSteps.size() == 0;
		};
	}
}

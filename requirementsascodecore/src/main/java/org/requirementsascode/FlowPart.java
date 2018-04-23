package org.requirementsascode;

import java.util.Objects;
import java.util.function.Predicate;

import org.requirementsascode.exception.ElementAlreadyInModel;
import org.requirementsascode.exception.NoSuchElementInModel;
import org.requirementsascode.flowposition.After;
import org.requirementsascode.flowposition.Anytime;
import org.requirementsascode.flowposition.FlowPosition;
import org.requirementsascode.flowposition.InsteadOf;

/**
 * Part used by the {@link ModelBuilder} to build a {@link Model}.
 *
 * @see Flow
 * @author b_muth
 */
public class FlowPart {
    private Flow flow;
    private UseCase useCase;
    private UseCasePart useCasePart;
    private FlowPosition optionalFlowPosition;
    private Predicate<ModelRunner> optionalWhen;

    FlowPart(Flow flow, UseCasePart useCasePart) {
	this.flow = flow;
	this.useCasePart = useCasePart;
	this.useCase = useCasePart.useCase();
    }

    /**
     * Creates a new step in this flow, with the specified name.
     *
     * @param stepName
     *            the name of the step to be created
     * @return the newly created step part, to ease creation of further steps
     * @throws ElementAlreadyInModel
     *             if a step with the specified name already exists in the use case
     */
    public StepPart step(String stepName) {
	FlowStep step = createStep(stepName);
	StepPart stepPart = new StepPart(step, useCasePart, this);
	return stepPart;
    }

    FlowStep createStep(String stepName) {
	FlowStep step;

	if (hasDefinedFlowPositionOrWhen()) {
	    step = useCase.newInterruptingFlowStep(stepName, flow, optionalFlowPosition, optionalWhen);
	} else {
	    step = useCase.newInterruptableFlowStep(stepName, flow);
	}

	return step;
    }

    private boolean hasDefinedFlowPositionOrWhen() {
	return optionalWhen != null || optionalFlowPosition != null;
    }

    /**
     * Starts the flow after the specified step has been run, in this flow's use
     * case. 
     * 
     * Note: You should use after to handle exceptions that occurred in the specified
     * step.
     *
     * @param stepName
     *            the name of the step to start the flow after
     * @return this use case flow part, to ease creation of the condition and the
     *         first step of the flow
     * @throws NoSuchElementInModel
     * 		if the specified step is not found in a flow of this use case
     * 		            
     */
    public FlowPart after(String stepName) {
	Step step = useCase.findStep(stepName);
	optionalFlowPosition = new After(step);
	return this;
    }

    /**
     * Starts the flow as an alternative to the specified step, in this flow's use
     * case.
     *
     * @param stepName
     *            the name of the specified step
     * @return this use case flow part, to ease creation of the condition and the
     *         first step of the flow
     * @throws NoSuchElementInModel
     *             if the specified step is not found in this flow's use case
     */
    public FlowPart insteadOf(String stepName) {
	FlowStep step = (FlowStep)useCase.findStep(stepName);
	optionalFlowPosition = new InsteadOf(step);
	return this;
    }

    /**
     * Starts the flow after any step that has been run, or at the beginning.
     * 
     * @return this use case flow part, to ease creation of the condition and the
     *         first step of the flow
     */
    public FlowPart anytime() {
	optionalFlowPosition = new Anytime();
	return this;
    }

    /**
     * Constrains the flow's condition: only if the specified condition is true as
     * well (beside the flow position), the flow is started.
     *
     * @param whenCondition
     *            the condition that constrains when the flow is started
     * @return this use case flow part, to ease creation of the condition and the
     *         first step of the flow
     */
    public FlowPart when(Predicate<ModelRunner> whenCondition) {
	Objects.requireNonNull(whenCondition);

	optionalWhen = whenCondition;
	return this;
    }

    Flow getFlow() {
	return flow;
    }

    UseCasePart getUseCasePart() {
	return useCasePart;
    }

    ModelBuilder getUseCaseModelBuilder() {
	return useCasePart.getUseCaseModelBuilder();
    }
}

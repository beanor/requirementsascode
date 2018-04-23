package org.requirementsascode;

import static org.junit.Assert.assertEquals;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

public class AdaptedSystemReactionTest extends AbstractTestCase {
    private String stepName;
    private Object event;

    @Before
    public void setup() {
	setupWith(new TestModelRunner());
    }

    @Test
    public void printsTextAndPerformsAdaptedSystemReaction() {
	useCaseModelRunner.adaptSystemReaction(withSavingStepNameAndEvent());
	stepName = "";

	Model useCaseModel = 
		useCaseModelBuilder.useCase(USE_CASE)
			.basicFlow()
				.step(SYSTEM_DISPLAYS_TEXT)
					.system(displaysConstantText())
		.build();

	useCaseModelRunner.run(useCaseModel);

	assertEquals(SYSTEM_DISPLAYS_TEXT, stepName);
	assertEquals(TestModelRunner.class, event.getClass());
    }

    private Consumer<SystemReactionTrigger> withSavingStepNameAndEvent() {
	return systemReactionTrigger -> {
	    stepName = systemReactionTrigger.getUseCaseStep().getName();
	    event = systemReactionTrigger.getEvent();
	    systemReactionTrigger.trigger();
	};
    }
}

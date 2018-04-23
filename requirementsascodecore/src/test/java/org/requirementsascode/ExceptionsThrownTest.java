package org.requirementsascode;

import static org.hamcrest.core.Is.isA;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.requirementsascode.exception.ElementAlreadyInModel;
import org.requirementsascode.exception.MissingUseCaseStepPart;
import org.requirementsascode.exception.MoreThanOneStepCanReact;
import org.requirementsascode.exception.NoSuchElementInModel;
import org.requirementsascode.exception.UnhandledException;

public class ExceptionsThrownTest extends AbstractTestCase {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
	setupWith(new TestModelRunner());
    }

    @Test
    public void throwsExceptionIfInsteadOfStepNotExistsInSameUseCase() {
	thrown.expect(NoSuchElementInModel.class);
	thrown.expectMessage(CUSTOMER_ENTERS_TEXT);

	useCaseModelBuilder.useCase(USE_CASE).basicFlow().insteadOf(CUSTOMER_ENTERS_TEXT);
    }

    @Test
    public void throwsExceptionIfAfterStepNotExistsInSameUseCase() {
	thrown.expect(NoSuchElementInModel.class);
	thrown.expectMessage(CUSTOMER_ENTERS_TEXT);

	useCaseModelBuilder.useCase(USE_CASE).basicFlow().after(CUSTOMER_ENTERS_TEXT);
    }

    @Test
    public void throwsExceptionIfContinueAfterNotExists() {
	thrown.expect(NoSuchElementInModel.class);
	thrown.expectMessage(CONTINUE);

	useCaseModelBuilder.useCase(USE_CASE).basicFlow().step("S1").continuesAfter(CONTINUE);
    }

    @Test
    public void throwsExceptionIfContinueAtNotExists() {
	thrown.expect(NoSuchElementInModel.class);
	thrown.expectMessage(CONTINUE);

	useCaseModelBuilder.useCase(USE_CASE).basicFlow().step("S1").continuesAt(CONTINUE);
    }

    @Test
    public void throwsExceptionIfContinueWithoutAlternativeAtNotExists() {
	thrown.expect(NoSuchElementInModel.class);
	thrown.expectMessage(CONTINUE);

	useCaseModelBuilder.useCase(USE_CASE).basicFlow().step("S1").continuesWithoutAlternativeAt(CONTINUE);
    }

    @Test
    public void throwsExceptionIfFlowIsCreatedTwice() {
	thrown.expect(ElementAlreadyInModel.class);
	thrown.expectMessage(ALTERNATIVE_FLOW);

	useCaseModelBuilder
		.useCase(USE_CASE)
			.flow(ALTERNATIVE_FLOW)
				.step(SYSTEM_DISPLAYS_TEXT).system(displaysConstantText())
			.flow(ALTERNATIVE_FLOW);
    }

    @Test
    public void throwsExceptionIfStepIsCreatedTwice() {
	thrown.expect(ElementAlreadyInModel.class);
	thrown.expectMessage(CUSTOMER_ENTERS_TEXT);

	useCaseModelBuilder
		.useCase(USE_CASE)
			.basicFlow()
				.step(CUSTOMER_ENTERS_TEXT).system(displaysConstantText())
				.step(CUSTOMER_ENTERS_TEXT).system(displaysConstantText());
    }

    @Test
    public void throwsExceptionIfMoreThanOneStepCanReactInSameUseCase() {
	thrown.expect(MoreThanOneStepCanReact.class);
	thrown.expectMessage(CUSTOMER_ENTERS_TEXT);
	thrown.expectMessage(CUSTOMER_ENTERS_ALTERNATIVE_TEXT);

	Model useCaseModel = useCaseModelBuilder
		.useCase(USE_CASE)
			.basicFlow().anytime()
				.step(CUSTOMER_ENTERS_TEXT).system(displaysConstantText())
			.flow(ALTERNATIVE_FLOW).anytime()
				.step(CUSTOMER_ENTERS_ALTERNATIVE_TEXT).system(displaysConstantText())
		.build();

	useCaseModelRunner.run(useCaseModel);
    }

    @Test
    public void throwsExceptionIfMoreThanOneStepCanReactInDifferentUseCases() {
	thrown.expect(MoreThanOneStepCanReact.class);
	thrown.expectMessage("Step 1");
	thrown.expectMessage("Step 2 with same event as Step 1");

	Model useCaseModel = useCaseModelBuilder
		.useCase("Use Case")
			.basicFlow()
				.step("Step 1").user(String.class).system(s -> System.out.println(s))
		.useCase("Another Use Case")
			.basicFlow()
				.step("Step 2 with same event as Step 1").user(String.class).system(s -> System.out.println(s))
		.build();

	useCaseModelRunner.run(useCaseModel);
	useCaseModelRunner.reactTo(new String("Some text"));
    }

    @Test
    public void throwsExceptionIfActorPartIsNotSpecified() {
	thrown.expect(MissingUseCaseStepPart.class);
	thrown.expectMessage(CUSTOMER_ENTERS_TEXT);

	useCaseModelBuilder.useCase(USE_CASE).basicFlow().step(CUSTOMER_ENTERS_TEXT);

	Model useCaseModel = useCaseModelBuilder.build();

	useCaseModelRunner.run(useCaseModel);
    }

    @Test
    public void throwsExceptionIfSystemPartIsNotSpecified() {
	thrown.expect(MissingUseCaseStepPart.class);
	thrown.expectMessage(CUSTOMER_ENTERS_TEXT);

	useCaseModelBuilder.useCase(USE_CASE).basicFlow().step(CUSTOMER_ENTERS_TEXT).as(customer)
		.user(EntersText.class);

	Model useCaseModel = useCaseModelBuilder.build();

	useCaseModelRunner.as(customer).run(useCaseModel);
	useCaseModelRunner.reactTo(entersText());
    }

    @Test
    public void throwsUncaughtExceptionIfExceptionIsNotHandled() {
	thrown.expect(UnhandledException.class);
	thrown.expectCause(isA(IllegalStateException.class));

	useCaseModelBuilder.useCase(USE_CASE).basicFlow().step(CUSTOMER_ENTERS_TEXT).system(r -> {
	    throw new IllegalStateException();
	});

	Model useCaseModel = useCaseModelBuilder.build();

	useCaseModelRunner.run(useCaseModel);
    }

    @Test
    public void throwsExceptionWhenIncludedUseCaseIsDeclaredLater() {
	thrown.expect(NoSuchElementInModel.class);
	thrown.expectMessage(INCLUDED_USE_CASE);

	useCaseModelBuilder
		.useCase(USE_CASE)
			.basicFlow()
				.step(SYSTEM_INCLUDES_USE_CASE).includesUseCase(INCLUDED_USE_CASE)
				.step(SYSTEM_DISPLAYS_TEXT).system(displaysConstantText())
		.useCase(INCLUDED_USE_CASE)
			.basicFlow()
				.step(SYSTEM_DISPLAYS_NUMBER).user(EntersNumber.class).system(displaysEnteredNumber())
		.build();
    }
}

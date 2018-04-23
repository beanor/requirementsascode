package hexagon.usecase;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.requirementsascode.TestModelRunner;
import org.requirementsascode.Model;

import hexagon.adapter.stub.RepositoryStub;
import hexagon.adapter.stub.WriterStub;
import hexagon.usecaserealization.FeelStuffUseCaseRealization;

public class FeelStuffUseCaseTest {

  private Model useCaseModel;
  private TestModelRunner testRunner;
  private FeelStuffUseCaseRealization feelStuffUseCaseRealization;

  @Before
  public void setUp() throws Exception {
    feelStuffUseCaseRealization =
        new FeelStuffUseCaseRealization(new WriterStub(), new RepositoryStub());
    useCaseModel =
        new HexagonUseCaseModel(feelStuffUseCaseRealization)
            .buildWith(Model.builder());
    testRunner = new TestModelRunner();
  }

  @Test
  public void testBasicFlow() {
    testRunner.run(useCaseModel);
    testRunner.reactTo(new AsksForPoem(), new AsksForPoem(), new AsksForPoem());
    assertEquals("1;2;3;", testRunner.getRunStepNames());
  }
}

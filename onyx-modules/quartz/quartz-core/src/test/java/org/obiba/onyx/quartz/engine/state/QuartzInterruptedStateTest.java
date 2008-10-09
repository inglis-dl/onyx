package org.obiba.onyx.quartz.engine.state;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.Serializable;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.ITransitionEventSink;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;

public class QuartzInterruptedStateTest {
  
  private ITransitionEventSink eventSinkMock;

  private QuestionnaireParticipantService questionnaireParticipantServiceMock;

  private ActiveInterviewService activeInterviewServiceMock;

  private QuartzInterruptedState interruptedState;

  /**
   * Tests that the questionnaireParticipant is deleted if stage canceled or not applicable
   */

  @Before
  public void setUp() {
    eventSinkMock = createMock(ITransitionEventSink.class);
    questionnaireParticipantServiceMock = createMock(QuestionnaireParticipantService.class);
    activeInterviewServiceMock = createMock(ActiveInterviewService.class);
  }

  @Test
  public void testQuestionnaireParticipantDeleteWhenCancel() {

    interruptedState = new QuartzInterruptedState() {
      @Override
      protected Boolean areDependenciesCompleted() {
        return true;
      }
    };

    setInterruptedState();

    expect(activeInterviewServiceMock.getParticipant()).andReturn(new Participant());
    expect(questionnaireParticipantServiceMock.getLastQuestionnaireParticipant((Participant) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(new QuestionnaireParticipant());
    questionnaireParticipantServiceMock.deleteQuestionnaireParticipant((Serializable) EasyMock.anyObject());
    eventSinkMock.castEvent(TransitionEvent.CANCEL);

    Action completeAction = new Action();

    replay(eventSinkMock);
    replay(activeInterviewServiceMock);
    replay(questionnaireParticipantServiceMock);

    interruptedState.stop(completeAction);

    verify(eventSinkMock);
    verify(activeInterviewServiceMock);
    verify(questionnaireParticipantServiceMock);
  }

  @Test
  public void testQuestionnaireParticipantDeleteWhenInvalid() {

    interruptedState = new QuartzInterruptedState() {
      @Override
      protected Boolean areDependenciesCompleted() {
        return null;
      }
    };

    setInterruptedState();

    expect(activeInterviewServiceMock.getParticipant()).andReturn(new Participant());
    expect(questionnaireParticipantServiceMock.getLastQuestionnaireParticipant((Participant) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(new QuestionnaireParticipant());
    questionnaireParticipantServiceMock.deleteQuestionnaireParticipant((Serializable) EasyMock.anyObject());
    eventSinkMock.castEvent(TransitionEvent.INVALID);

    Action completeAction = new Action();

    replay(eventSinkMock);
    replay(activeInterviewServiceMock);
    replay(questionnaireParticipantServiceMock);

    interruptedState.stop(completeAction);

    verify(eventSinkMock);
    verify(activeInterviewServiceMock);
    verify(questionnaireParticipantServiceMock);
  }

  @Test
  public void testQuestionnaireParticipantDeleteWhenNotApplicable() {
    
    interruptedState = new QuartzInterruptedState();
    setInterruptedState();
    
    expect(activeInterviewServiceMock.getParticipant()).andReturn(new Participant());
    expect(questionnaireParticipantServiceMock.getLastQuestionnaireParticipant((Participant) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(new QuestionnaireParticipant());
    questionnaireParticipantServiceMock.deleteQuestionnaireParticipant((Serializable) EasyMock.anyObject());
    
    replay(activeInterviewServiceMock);
    replay(questionnaireParticipantServiceMock);

    // testing when notApplicable Transition event is passed: deletion should happen
    interruptedState.wantTransitionEvent(TransitionEvent.NOTAPPLICABLE);
    
    // testing when another Transition event is passed: NO deletion should happen
    interruptedState.wantTransitionEvent(TransitionEvent.INVALID);
    
    verify(activeInterviewServiceMock);
    verify(questionnaireParticipantServiceMock);
  }
  
  private void setInterruptedState() {
    interruptedState.setStage(newTestStage());
    interruptedState.setEventSink(eventSinkMock);
    interruptedState.setQuestionnaireParticipantService(questionnaireParticipantServiceMock);
    interruptedState.setActiveInterviewService(activeInterviewServiceMock);
  }

  private Stage newTestStage() {
    Stage s = new Stage();
    s.setName("QUE1");
    return (s);
  }
}

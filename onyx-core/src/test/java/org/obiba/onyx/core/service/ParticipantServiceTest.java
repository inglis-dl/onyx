package org.obiba.onyx.core.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.ObjectError;

@Transactional
public class ParticipantServiceTest extends BaseDefaultSpringContextTestCase {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ParticipantServiceTest.class);

  @Autowired(required = true)
  PersistenceManager persistenceManager;

  @Autowired(required = true)
  ParticipantService participantService;

  @Test
  @Dataset
  public void testParticipantByCode() {
    Assert.assertEquals(1, participantService.countParticipantsByCode("1"));
    Assert.assertEquals(2, participantService.countParticipantsByCode("100002"));
  }

  @Test
  @Dataset
  public void testParticipantByInterviewStatus() {
    Assert.assertEquals(1, participantService.countParticipants(InterviewStatus.COMPLETED));
    Assert.assertEquals(1, participantService.countParticipants(InterviewStatus.IN_PROGRESS));
  }

  @Test
  @Dataset
  public void testParticipantByName() {
    Assert.assertEquals(2, participantService.countParticipantsByName("Hudson"));
    Assert.assertEquals(1, participantService.countParticipantsByName("John Hudson"));
    Assert.assertEquals(2, participantService.countParticipantsByName("ohn"));
  }

  @Test
  @Dataset
  public void testParticipantByAppointmentDate() {
    Calendar cal = Calendar.getInstance();
    cal.set(2008, 8, 1, 0, 0, 0);
    Date from = cal.getTime();
    cal.add(Calendar.DAY_OF_MONTH, 1);
    Date to = cal.getTime();

    log.info("from=" + from + " to=" + to);
    Assert.assertEquals(2, participantService.countParticipants(from, to));
  }

  @Test
  @Dataset(filenames={"AppConfigurationForParticipantServiceTest.xml"})
  public void testParticipantReader() {
    try {
      participantService.updateParticipants(getClass().getResourceAsStream("rendez-vous.xls"));
      Assert.assertEquals(3l, persistenceManager.match(new Participant()).size());

      // test we can run same file multiple times without breaking the db
      participantService.updateParticipants(getClass().getResourceAsStream("rendez-vous.xls"));
      Assert.assertEquals(3l, persistenceManager.match(new Participant()).size());
      Participant p = new Participant();
      p.setEnrollmentId("100001");
      p = persistenceManager.matchOne(p);
      Assert.assertNotNull("Cannot find participant", p);
      Assert.assertNotNull("Cannot find participant appointment", p.getAppointment());
      Assert.assertNotNull("Cannot find participant appointment date", p.getAppointment().getDate());

      // add a completed interview
      p.setBarcode("1");
      persistenceManager.save(p);
      Interview interview = new Interview();
      interview.setStatus(InterviewStatus.COMPLETED);
      interview.setParticipant(p);
      persistenceManager.save(interview);
      p.setInterview(interview);
      persistenceManager.save(p);
      p = new Participant();
      p.setEnrollmentId("100001");
      p = persistenceManager.matchOne(p);
      Assert.assertNotNull("Cannot find participant", p);
      Assert.assertNotNull("Cannot find participant interview", p.getInterview());
      Assert.assertEquals("Cannot find participant completed interview", InterviewStatus.COMPLETED, p.getInterview().getStatus());

      participantService.updateParticipants(getClass().getResourceAsStream("rendez-vous.xls"));
      Assert.assertEquals(3l, persistenceManager.match(new Participant()).size());
      p = new Participant();
      p.setEnrollmentId("100001");
      p = persistenceManager.matchOne(p);
      Assert.assertNotNull("Cannot find participant", p);
      Assert.assertNotNull("Cannot find participant appointment", p.getAppointment());
      Assert.assertNotNull("Cannot find participant appointment date", p.getAppointment().getDate());
      Assert.assertNotNull("Cannot find participant interview", p.getInterview());
      Assert.assertEquals("Cannot find participant completed interview", InterviewStatus.COMPLETED, p.getInterview().getStatus());

      try {
        participantService.updateParticipants(getClass().getResourceAsStream("rendez-vous-corrupted.xls"));
        Assert.fail("ValidationRuntimeException not thrown");
      } catch(ValidationRuntimeException ve) {
        List<ObjectError> oes = ve.getAllObjectErrors();
        Assert.assertEquals("Not the right count of errors", 2, oes.size());
        ObjectError oe = oes.get(0);
        Assert.assertEquals("Not the right error code", "ParticipantInterviewCompletedWithAppointmentInTheFuture", oe.getCode());
        Assert.assertEquals("Not the right error arguments count", 2, oe.getArguments().length);
        Assert.assertEquals("Not the right error argument line", "3", oe.getArguments()[0]);
        Assert.assertEquals("Not the right error argument id", "100001", oe.getArguments()[1]);
        oe = oes.get(1);
        Assert.assertEquals("Not the right error code", "WrongParticipantSiteName", oe.getCode());
        Assert.assertEquals("Not the right error arguments count", 4, oe.getArguments().length);
        Assert.assertEquals("Not the right error argument line", "10", oe.getArguments()[0]);
        Assert.assertEquals("Not the right error argument id", "100008", oe.getArguments()[1]);
        Assert.assertEquals("Not the right error argument participant site name", "cag002", oe.getArguments()[2]);
        Assert.assertEquals("Not the right error argument app site name", "cag001", oe.getArguments()[3]);
        log.info(ve.toString());
      }

    } catch(ValidationRuntimeException e) {
      Assert.fail(e.getMessage());
      e.printStackTrace();
    }
  }
}

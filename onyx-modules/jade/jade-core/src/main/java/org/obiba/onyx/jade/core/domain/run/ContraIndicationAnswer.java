/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.run;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.ContraIndication;

@Entity
public class ContraIndicationAnswer extends AbstractEntity {

  private static final long serialVersionUID = 112312312323L;

  private ContraIndication contraIndication;

  private Boolean contraIndicated;

  @ManyToOne
  @JoinColumn(name = "participant_id")
  private Participant participant;

  public ContraIndication getContraIndication() {
    return contraIndication;
  }

  public ContraIndicationAnswer(ContraIndication contraIndication) {
    this.contraIndication = contraIndication;
  }

  public void setContraIndication(ContraIndication contraIndication) {
    this.contraIndication = contraIndication;
  }

  public Boolean getContraIndicated() {
    return contraIndicated;
  }

  public void setContraIndicated(Boolean contraIndicated) {
    this.contraIndicated = contraIndicated;
  }

  public Participant getParticipant() {
    return participant;
  }

  public void setParticipant(Participant participant) {
    this.participant = participant;
  }

}

/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.panel;

import java.io.Serializable;

import org.obiba.onyx.engine.ModuleRegistry;

/**
 * Simulates ModuleRegistry with Serializable to avoid NotSerializableException in the unit test
 */
public class ModuleRegistryStub extends ModuleRegistry implements Serializable {

  private static final long serialVersionUID = 1L;

}

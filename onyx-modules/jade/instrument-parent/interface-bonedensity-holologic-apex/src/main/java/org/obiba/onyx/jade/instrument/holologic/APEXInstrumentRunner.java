/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.holologic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.tool.dcmrcv.ApexTag;
import org.dcm4che2.tool.dcmrcv.DicomServer;
import org.dcm4che2.tool.dcmrcv.DicomServer.StoredDicomFile;
import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.holologic.IVAImagingScanDataExtractor.Energy;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.FileUtil;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.util.FileSystemUtils;

public class APEXInstrumentRunner implements InstrumentRunner, InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(APEXInstrumentRunner.class);

  // Injected by spring.
  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;

  private JdbcTemplate patScanDb;

  private String patScanDbPath;

  private File scanDataDir;

  private DicomSettings dicomSettings;

  private DicomServer server;

  private File dcmDir;

  // participant data
  private String participantID;

  private List<String> participantFiles = new ArrayList<String>();

  private Set<String> outVendorNames;

  private ResourceBundle apexResourceBundle;

  private Locale locale;

  private List<String> sentVariables = new ArrayList<String>();

  @Override
  public void afterPropertiesSet() throws Exception {
    setApexResourceBundle(ResourceBundle.getBundle("instrument", getLocale()));
  }

  /**
   * Retrieve participant data from the database and write them in the patient scan database
   * @throws Exception
   */
  public void initParticipantData() {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
    participantID = "ONYX-" + formatter.format(new Date());
    final Double weight = instrumentExecutionService.getInputParameterValue("Weight").getValue();
    final Double height = instrumentExecutionService.getInputParameterValue("Height").getValue();

    patScanDb.update("insert into PATIENT ( PATIENT_KEY, IDENTIFIER1, WEIGHT, HEIGHT ) values( ?, ?, ?, ? )", new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, participantID);
        ps.setString(2, participantID);
        ps.setDouble(3, weight);
        ps.setDouble(4, height);
      }
    });

    if(instrumentExecutionService.hasInputParameter("Gender")) {
      final String gender = instrumentExecutionService.getInputParameterValue("Gender").getValue();
      patScanDb.update("update PATIENT set SEX = ? where PATIENT_KEY = ?", new PreparedStatementSetter() {
        public void setValues(PreparedStatement ps) throws SQLException {
          if(gender.toUpperCase().startsWith("M")) {
            ps.setString(1, "M");
          } else {
            ps.setString(1, "F");
          }
          ps.setString(2, participantID);
        }
      });
    }

    if(instrumentExecutionService.hasInputParameter("DateOfBirth")) {
      Date dob = instrumentExecutionService.getInputParameterValue("DateOfBirth").getValue();
      final java.sql.Date sqlDob = new java.sql.Date(dob.getTime());
      patScanDb.update("update PATIENT set BIRTHDATE = ? where PATIENT_KEY = ?", new PreparedStatementSetter() {
        public void setValues(PreparedStatement ps) throws SQLException {
          ps.setDate(1, sqlDob);
          ps.setString(2, participantID);
        }
      });
    }
  }

  /**
   * Initialise or restore instrument data (database and scan files).
   * @param starting
   * @throws Exception
   */
  protected void resetDeviceData(boolean starting) {
    File backupDbFile = new File(patScanDbPath + ".orig");
    File currentDbFile = new File(patScanDbPath);
    scanDataDir = currentDbFile.getParentFile();

    try {
      if(backupDbFile.exists() && starting) {
        log.error("Backup file exists and you are starting a scan: probably abnormal shutdown");
        log.info("Restoring backed up file");
        FileUtil.copyFile(backupDbFile, currentDbFile);
      }
      if(starting) {
        FileUtil.copyFile(currentDbFile, backupDbFile);
      } else {
        if(backupDbFile.exists()) {
          restore(backupDbFile, currentDbFile);
        }
      }
    } catch(Exception ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException("Error while reseting device data: " + ex.getMessage(), ex);
    }
  }

  private void restore(File backupDbFile, File currentDbFile) throws IOException {
    FileUtil.copyFile(backupDbFile, currentDbFile);
    backupDbFile.delete();
    // delete scan files
    for(String fileName : participantFiles) {
      File file = new File(scanDataDir, fileName);
      if(file.exists()) {
        file.delete();
      }
    }
  }

  private List<Map<String, Data>> retrieveDeviceData() {

    List<Map<String, Data>> dataList = new ArrayList<Map<String, Data>>();

    if(instrumentExecutionService.hasInputParameter("HipSide")) {
      String hipSide = instrumentExecutionService.getInputParameterValue("HipSide").getValue();
      if(hipSide != null) {
        if(hipSide.toUpperCase().startsWith("L")) {
          extractLeftHip(dataList);
        } else if(hipSide.toUpperCase().startsWith("R")) {
          extractRightHip(dataList);
        } else if(hipSide.toUpperCase().startsWith("B")) {
          if(instrumentExecutionService.getExpectedMeasureCount() > 1) {
            extractLeftHip(dataList);
            extractRightHip(dataList);
          } else {
            extractScanData(dataList, new HipScanDataExtractor(patScanDb, scanDataDir, participantID, Side.LEFT, server));
            extractScanData(dataList, new HipScanDataExtractor(patScanDb, scanDataDir, participantID, Side.RIGHT, server));
          }
        }
      }
    } else if(instrumentExecutionService.getExpectedMeasureCount() > 1) {
      extractLeftHip(dataList);
      extractRightHip(dataList);
    } else {
      extractScanData(dataList, new HipScanDataExtractor(patScanDb, scanDataDir, participantID, Side.LEFT, server));
      extractScanData(dataList, new HipScanDataExtractor(patScanDb, scanDataDir, participantID, Side.RIGHT, server));
    }
    if(instrumentExecutionService.hasInputParameter("ForearmSide")) {
      String forearmSide = instrumentExecutionService.getInputParameterValue("ForearmSide").getValue();
      if(forearmSide != null) {
        if(forearmSide.toUpperCase().startsWith("L")) {
          extractScanData(dataList, new ForearmScanDataExtractor(patScanDb, scanDataDir, participantID, Side.LEFT, server) {
            @Override
            public String getName() {
              return "FA";
            }
          });
        } else if(forearmSide.toUpperCase().startsWith("R")) {
          extractScanData(dataList, new ForearmScanDataExtractor(patScanDb, scanDataDir, participantID, Side.RIGHT, server) {
            @Override
            public String getName() {
              return "FA";
            }
          });
        }
      }
    } else {
      extractScanData(dataList, new ForearmScanDataExtractor(patScanDb, scanDataDir, participantID, Side.LEFT, server));
      extractScanData(dataList, new ForearmScanDataExtractor(patScanDb, scanDataDir, participantID, Side.RIGHT, server));
    }
    extractScanData(dataList, new WholeBodyScanDataExtractor(patScanDb, scanDataDir, participantID, server));
    extractScanData(dataList, new IVAImagingScanDataExtractor(patScanDb, scanDataDir, participantID, Energy.CLSA_DXA, server));

    return dataList;

  }

  private void extractRightHip(List<Map<String, Data>> dataList) {
    extractScanData(dataList, new HipScanDataExtractor(patScanDb, scanDataDir, participantID, Side.RIGHT, server) {
      @Override
      public String getName() {
        return "HIP";
      }
    });
  }

  private void extractLeftHip(List<Map<String, Data>> dataList) {
    extractScanData(dataList, new HipScanDataExtractor(patScanDb, scanDataDir, participantID, Side.LEFT, server) {
      @Override
      public String getName() {
        return "HIP";
      }
    });
  }

  private void extractScanData(List<Map<String, Data>> dataList, APEXScanDataExtractor extractor) {
    // filter the values to output
    Map<String, Data> extractedData = extractor.extractData();
    Map<String, Data> outputData = new HashMap<String, Data>();
    for(Entry<String, Data> entry : extractedData.entrySet()) {
      if(outVendorNames.contains(entry.getKey())) {
        outputData.put(entry.getKey(), entry.getValue());
      }
    }
    dataList.add(outputData);

    participantFiles.addAll(extractor.getFileNames());
  }

  public void sendDataToServer(Map<String, Data> data) {
    instrumentExecutionService.addOutputParameterValues(data);
  }

  /**
   * Implements parent method initialize from InstrumentRunner Delete results from previous measurement and initiate the
   * input file to be read by the external application
   */
  public void initialize() {
    if(externalAppHelper.isSotfwareAlreadyStarted()) {
      JOptionPane.showMessageDialog(null, externalAppHelper.getExecutable() + " already lock for execution.  Please make sure that another instance is not running.", "Cannot start application!", JOptionPane.ERROR_MESSAGE);
      throw new RuntimeException("already lock for execution");
    }
    showProcessingDialog();
    log.info("Backup local database");
    resetDeviceData(true);

    log.info("Setting participant data");
    initParticipantData();

    outVendorNames = instrumentExecutionService.getExpectedOutputParameterVendorNames();

    try {
      File tmpDir = File.createTempFile("dcm", "");
      if(tmpDir.delete() == false || tmpDir.mkdir() == false) {
        throw new RuntimeException("Cannot create temp directory");
      }
      this.dcmDir = tmpDir;
      log.info("DICOM files stored to {}", dcmDir.getAbsolutePath());
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
    this.server = new DicomServer(dcmDir, dicomSettings);
  }

  /**
   * Implements parent method run from InstrumentRunner Launch the external application, retrieve and send the data
   */
  public void run() {
    log.info("Start Dicom server");
    try {
      server.start();
    } catch(IOException e) {
      log.error("Error start server");
    }

    log.info("Launching APEX software");
    externalAppHelper.launchExternalSoftware();

    retrieveMeasurements();
  }

  private void retrieveMeasurements() {
    log.info("Retrieving measurements");
    List<Map<String, Data>> dataList = retrieveDeviceData();

    log.info("Sending data to server");
    sentVariables.clear();
    for(Map<String, Data> dataMap : dataList) {
      for(String key : dataMap.keySet()) {
        sentVariables.add(key);
      }
      sendDataToServer(dataMap);
    }
  }

  /**
   * Implements parent method shutdown from InstrumentRunner Delete results from current measurement
   */
  public void shutdown() {
    StringBuilder warningMessage = new StringBuilder();
    boolean completeVariable = isCompleteVariable();
    boolean completeDicom = isCompleteDicom();
    if(completeVariable == false) {
      warningMessage.append("Your scan is incomplete, you probably did not perform scan, analysis and/or sent Dicom file(s)\n\n");
    }
    if(completeDicom == false) {
      warningMessage.append("Your scan is incomplete, P and R files are not included in Dicom file(s).\n\n");
    }
    warningMessage.append("Click 'Yes' to re-launch Apex,\n");
    warningMessage.append("Click 'No' to cancel measurement and delete participant data.\n");

    if(completeVariable && completeDicom) {
      cleanAllData();
    } else {
      showWarningIncompleteMeasure(warningMessage);
    }
  }

  private void cleanAllData() {
    log.info("Restoring local database and cleaning scan files");
    resetDeviceData(false);

    log.info("Shutdown Dicom server");
    server.stop();

    deleteTemporaryDicomFiles();
  }

  private void showWarningIncompleteMeasure(StringBuilder warningMessage) {
    JOptionPane pane = new JOptionPane(warningMessage, JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
    JDialog dialog = pane.createDialog("Warning");
    dialog.setAlwaysOnTop(true);
    dialog.setVisible(true);

    switch(processJOptionPaneValue(pane.getValue())) {
    case JOptionPane.CLOSED_OPTION:
      showWarningIncompleteMeasure(warningMessage);
      break;
    case JOptionPane.YES_OPTION:
      deleteTemporaryDicomFiles();
      log.info("Re-Launching APEX software");
      externalAppHelper.launchExternalSoftware();
      retrieveMeasurements();
      shutdown();
      break;
    case JOptionPane.NO_OPTION:
      cleanAllData();
      break;
    }
  }

  private int processJOptionPaneValue(Object value) {
    if(value == null) return JOptionPane.CLOSED_OPTION;
    if(value instanceof Integer) return ((Integer) value).intValue();
    return JOptionPane.CLOSED_OPTION;
  }

  /**
   * Return true if you sent all required variable, false otherwise
   * @return
   */
  private boolean isCompleteVariable() {
    List<String> missing = new ArrayList<String>();
    boolean retValue = true;
    for(String out : outVendorNames) {
      if(sentVariables.contains(out) == false) {
        missing.add(out);
        retValue = false;
      }
    }
    log.info("Missing variables: " + missing);
    return retValue;
  }

  /**
   * Return true if Dicom contains P and R files, false otherwise
   * @return
   */
  private boolean isCompleteDicom() {
    for(StoredDicomFile file : server.listDicomFiles()) {
      for(ApexTag tag : ApexTag.values())
        try {
          DicomObject dicomObject = file.getDicomObject();
          if(dicomObject.contains(tag.getValue())) {
            if(dicomObject.containsValue(tag.getValue()) == false) {
              log.info("Missing P and/or R files");
              return false;
            }
          }
        } catch(IOException e) {
        }
    }
    return true;
  }

  private void deleteTemporaryDicomFiles() {
    log.info("Delete temporary dicom files");
    FileSystemUtils.deleteRecursively(dcmDir);
  }

  private void showProcessingDialog() {

    JPanel messagePanel = new JPanel();
    messagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));

    JLabel message = new JLabel(apexResourceBundle.getString("Message.ProcessingMeasurement"));
    message.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
    messagePanel.add(message);

    JLabel subMessage = new JLabel(apexResourceBundle.getString("Message.ProcessingMeasurementInstructions"));
    subMessage.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
    subMessage.setForeground(Color.RED);
    messagePanel.add(subMessage);

    JDialog dialog = new JDialog();
    dialog.setResizable(false);
    dialog.add(messagePanel);
    dialog.pack();
    dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    // Make sure dialog stays on top of all other application windows.
    dialog.setLocationByPlatform(true);

    // Center dialog horizontally at the bottom of the screen.
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    dialog.setLocation((screenSize.width - dialog.getWidth()) / 2, screenSize.height - dialog.getHeight() - 70);

    dialog.setVisible(true);
    dialog.setResizable(false);

  }

  public enum Side {
    LEFT, RIGHT
  }

  public void setInstrumentExecutionService(InstrumentExecutionService instrumentExecutionService) {
    this.instrumentExecutionService = instrumentExecutionService;
  }

  public void setExternalAppHelper(ExternalAppLauncherHelper externalAppHelper) {
    this.externalAppHelper = externalAppHelper;
  }

  public void setPatScanDb(JdbcTemplate patScanDb) {
    this.patScanDb = patScanDb;
  }

  public void setPatScanDbPath(String patScanDbPath) {
    this.patScanDbPath = patScanDbPath;
    this.scanDataDir = new File(patScanDbPath).getParentFile();
  }

  public void setDicomSettings(DicomSettings dicomSettings) {
    this.dicomSettings = dicomSettings;
  }

  public void setApexResourceBundle(ResourceBundle apexResourceBundle) {
    this.apexResourceBundle = apexResourceBundle;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

}

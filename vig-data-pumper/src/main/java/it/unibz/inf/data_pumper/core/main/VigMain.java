package it.unibz.inf.data_pumper.core.main;

/*
 * #%L
 * dataPumper
 * %%
 * Copyright (C) 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

//import org.apache.log4j.BasicConfigurator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import it.unibz.inf.data_pumper.columns.ColumnPumper;
import it.unibz.inf.data_pumper.configuration.ConfParser;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.connection.InstanceNullException;
import it.unibz.inf.data_pumper.core.main.options.Conf;
import it.unibz.inf.data_pumper.core.main.options.VigOptionsInterface;
import it.unibz.inf.data_pumper.persistence.LogToFile;
import it.unibz.inf.data_pumper.persistence.statistics.xml_model.*;
import it.unibz.inf.data_pumper.tables.Schema;
import it.unibz.inf.data_pumper.utils.ExecutionStatisticsProfiler;
import it.unibz.inf.vig_mappings_analyzer.core.utils.QualifiedName;
import it.unibz.inf.vig_options.core.Option;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;


public class VigMain extends VigOptionsInterface {

  public static Logger logger = Logger.getLogger(VigMain.class.getCanonicalName());

  public static enum PumperType {
    DB("DB"), OBDA("OBDA");

    private String text;

    PumperType(String typeText) {
      this.text = typeText;
    }

    public String getText() {
      return this.text;
    }

    public static PumperType fromString(String text) {
      if (text != null) {
        for (PumperType b : PumperType.values()) {
          if (text.equalsIgnoreCase(b.text)) {
            return b;
          }
        }
      }
      return null;
    }
  }

  // Xml Model of the Data
  private static DatabaseModelCreator dbModelCreator;

  public static void main(String[] args) {

    BasicConfigurator.configure(); // log4j

    // Read command-line parameters and configuration file
    Conf conf = configure(args);

    // Init DB connection
    DBMSConnection.initInstance(conf.jdbcConnector(), conf.dbUrl(), conf.dbUser(), conf.dbPwd());

    DatabasePumper pumper = conf.pumperType() == PumperType.DB ? new DatabasePumperDB(conf) : new DatabasePumperOBDA(conf);
    pumper.pumpDatabase(conf.scale());

    // Stats
    ExecutionStatisticsProfiler.printStats();

    // Create xml model TODO: Do this
//    dbModelCreator = DatabaseModelCreator.INSTANCE;
//    DatabaseModel model = dbModelCreator.createXmlModel(conf.scale());
//    <></>ry {
//    dbModelCreator.printModelToFile(model);
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
  }

  /**
   * Read command-line parameters and configuration file
   *
   * @return
   **/
  private static Conf configure(String[] args) {
    // --- configuration -- //
    Option.parseOptions(args);
    // PumperType.valueOf(...

    ConfParser cP = ConfParser.getInstance(optRes.getValue());
    // Priority: shell - conf - shell-default
    Conf conf = null;
    try {
      conf = new Conf(
              optRes.getValue(),
              optJdbcConnector.parsed() ? optJdbcConnector.getValue() : cP.jdbcConnector().equals("") ? optJdbcConnector.getValue() : cP.jdbcConnector(),
              optDbUrl.parsed() ? optDbUrl.getValue() : cP.dbUrl(),
              optDbUser.parsed() ? optDbUser.getValue() : cP.dbUser(),
              optDbPwd.parsed() ? optDbPwd.getValue() : cP.dbPwd(),
              optRandomGen.parsed() ? optRandomGen.getValue() : cP.pureRandomGeneration().equals("") ? optRandomGen.getValue() : Helpers.parseBool(cP.pureRandomGeneration()),
              optMappingsFile.parsed() ? optMappingsFile.getValue() : cP.mappingsFile(),
              optMode.parsed() ? PumperType.fromString(optMode.getValue()) : cP.mode().equals("") ? PumperType.fromString(optMode.getValue()) : PumperType.fromString(cP.mode()),
              optFixedList.parsed() ? Helpers.parseListToQualifiedNames(optFixedList.getValue()) : Helpers.parseListToQualifiedNames(cP.fixed()),
              optNonFixedList.parsed() ? Helpers.parseListToQualifiedNames(optNonFixedList.getValue()) : Helpers.parseListToQualifiedNames(cP.fixed()),
              optCCAnalysisTimeout.parsed() ? optCCAnalysisTimeout.getValue() : cP.ccAnalysisTimeout().equals("") ? optCCAnalysisTimeout.getValue() : Helpers.parseInt(cP.ccAnalysisTimeout()),
              optScaling.parsed() ? optScaling.getValue() : cP.scale().equals("") ? optScaling.getValue() : (Double.parseDouble(cP.scale())),
              optTables.parsed() ? Helpers.parseListToQualifiedNames(optTables.getValue()) : Helpers.parseListToQualifiedNames(cP.tables()),
              optColumns.parsed() ? Helpers.parseListToQualifiedNames(optColumns.getValue()) : Helpers.parseListToQualifiedNames(cP.columns())
      );
    } catch (IOException e) {
      VigMain.closeEverythingAndExit(e);
    }
    assert conf != null : "Null configuration";
    return conf;
  }

  public static void closeEverythingAndExit() {
    try {
      DBMSConnection.getInstance().close();
    } catch (InstanceNullException e) {
      e.printStackTrace();
    } finally {
      LogToFile.getInstance().closeFile();
    }
    throw new RuntimeException();
  }

  public static void closeEverythingAndExit(Exception e) {
    try {
      DBMSConnection.getInstance().close();
    } catch (InstanceNullException e1) {
      e.printStackTrace();
    } finally {
      LogToFile.getInstance().closeFile();
    }
    throw new RuntimeException(e);
  }

  public static void closeEverythingAndExit(String msg, Exception e) {
    try {
      DBMSConnection.getInstance().close();
    } catch (InstanceNullException e1) {
      e.printStackTrace();
    } finally {
      LogToFile.getInstance().closeFile();
    }
    throw new RuntimeException(e);
  }

  private static class Helpers {
    private static boolean parseBool(String b) {
      return Boolean.parseBoolean(b);
    }

    private static int parseInt(String i) {
      return Integer.valueOf(i);
    }

    private static List<String> parseListToStrings(String listS) {
      List<String> result = Arrays.asList(listS.split("\\s+"));
      return result;
    }

    private static List<QualifiedName> parseListToQualifiedNames(String listS) {
      List<String> list = parseListToStrings(listS);
      List<QualifiedName> result =
              list.stream()
                      .filter(s -> !s.equals(""))
                      .map(s -> QualifiedName.makeFromDotSeparated(s))
                      .collect(Collectors.toList());
      return result;
    }
  }
};

abstract class DatabaseModelCreatorConsts {
  static final String XML_MODEL_FILE_NAME = "src/main/resources/npd.xml";
}

// This class should be singleton
enum DatabaseModelCreator {

  INSTANCE;

  // Internal state
  private final List<Schema> schemas;
  private final DBMSConnection dbOriginal;


  private DatabaseModelCreator() {
    schemas = new ArrayList<>();
    this.dbOriginal = DBMSConnection.getInstance();
    for (String tableName : dbOriginal.getAllTableNames()) {
      Schema schema = dbOriginal.getSchema(tableName);
      schemas.add(schema);
    }
  }


  DatabaseModel createXmlModel(double scaleFactor) {

    DatabaseModel result = new DatabaseModel();

    // Beans
    List<TableModel> tables = new ArrayList<>();
    List<SharingValues> sharing = new ArrayList<>();

    // Internal helpers
    TableModelMaker tMM = new TableModelMaker();

    for (Schema s : this.getSchemas()) {
      TableModel tM = tMM.makeTableModel(s);
      tables.add(tM);
      SharingValues sV = makeSharingValues(s);
      sharing.add(sV);
    }
    TableModelsContainer tMC = new TableModelsContainer();
    SharingValuesContainer sVC = new SharingValuesContainer();
    tMC.setTablesList(tables);
    sVC.setSharingList(sharing);

    // Set the result object
    result.setTables(tMC);
    result.setSharing(sVC);
    result.setScaleFactor(scaleFactor);

    return result;
  }


  private SharingValues makeSharingValues(
          Schema s) {
    // TODO Auto-generated method stub
    return null;
  }


  void printModelToFile(DatabaseModel model) throws IOException {
    jaxbObjectToXML(model);
  }

  private void jaxbObjectToXML(DatabaseModel model) {

    try {
      JAXBContext context = JAXBContext.newInstance(DatabaseModel.class);
      Marshaller m = context.createMarshaller();
      //for pretty-print XML in JAXB
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      // Write to System.out for debugging
      // m.marshal(emp, System.out);

      // Write to File
      m.marshal(model, new File(DatabaseModelCreatorConsts.XML_MODEL_FILE_NAME));
    } catch (JAXBException e) {
      e.printStackTrace();
    }
  }

  // Standard getters

  private List<Schema> getSchemas() {
    return this.schemas;
  }

  class TableModelMaker {

    TableModel makeTableModel(
            Schema s) {

      TableModel result = new TableModel();
      result.setName(s.getTableName());

      List<ColumnModel> cols = makeColumns(s);
      result.setColumns(cols);

      return result;
    }

    private List<ColumnModel> makeColumns(Schema s) {
      class ColumnModelMaker {
        ColumnModel makeColumnModel(ColumnPumper<?> cP) {
          ColumnModel result = new ColumnModel();

          // TODO : Case "not Fixed"
          result.setName(cP.getName());
          result.setDupsRatio(cP.getDuplicateRatio());
          result.setNullsRatio(cP.getNullRatio());
          return result;
        }
      }
      ;

      List<ColumnModel> result = new ArrayList<ColumnModel>();

      ColumnModelMaker cMM = new ColumnModelMaker();
      for (ColumnPumper<?> cP : s.getColumns()) {
        result.add(cMM.makeColumnModel(cP));
      }

      return result;
    }
  }
}

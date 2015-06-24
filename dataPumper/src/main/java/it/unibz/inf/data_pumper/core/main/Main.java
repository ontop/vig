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
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import it.unibz.inf.data_pumper.basic_datatypes.Schema;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.configuration.Conf;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.core.statistics.xml_model.*;
import it.unibz.inf.data_pumper.utils.Statistics;
import it.unibz.inf.vig_options.core.DoubleOption;
import it.unibz.inf.vig_options.core.Option;
import it.unibz.inf.vig_options.core.StringOption;
import it.unibz.inf.vig_options.ranges.DoubleRange;

import org.apache.log4j.BasicConfigurator;

enum PumperType{
	DB, OBDA
}

public class Main {
    
    private static PumperType pumperType;
    
    // configuration file
    private static Conf conf;

    // Options
    private static DoubleOption optScaling = new DoubleOption("--scale", "It specifies the scaling factor", "PUMPER", 1, new DoubleRange(0, Double.MAX_VALUE, false, true));	
    public static StringOption optResources = new StringOption("--res", "Location of the resources directory", "CONFIGURATION", "src/main/resources");

    // Xml Model of the Data
    private static DatabaseModelCreator dbModelCreator;
    
    public static void main(String[] args) {

	// --- configuration -- //
	BasicConfigurator.configure();		
	Option.parseOptions(args);
	double percentage = optScaling.getValue();
	conf = Conf.getInstance();
	boolean randomGen = false;
	try {
	    randomGen = conf.pureRandomGeneration();
	    pumperType = PumperType.valueOf(conf.pumperType());
	    DBMSConnection.initInstance(conf.jdbcConnector(), conf.dbUrl(), conf.dbUser(), conf.dbPwd());
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	DatabasePumper pumper = pumperType == PumperType.DB ? new DatabasePumperDB() : new DatabasePumperOBDA();

	if( randomGen ){
	    pumper.setPureRandomGeneration();
	}	
	pumper.pumpDatabase(percentage);

	Statistics.printStats();
	
	// Create xml model
	dbModelCreator = DatabaseModelCreator.INSTANCE;
	DatabaseModel model = dbModelCreator.createXmlModel(percentage);
	try {
	    dbModelCreator.printModelToFile(model);
	} catch (IOException e) {
	    e.printStackTrace();
	} 
    }
    
};

abstract class DatabaseModelCreatorConsts{
    static final String XML_MODEL_FILE_NAME = "src/main/resources/npd.xml";
}

// This class should be singleton
enum DatabaseModelCreator{
    
    INSTANCE;
    
    // Internal state 
    private final List<Schema> schemas;
    private final DBMSConnection dbOriginal; 
    
    
    private DatabaseModelCreator() {
	schemas = new ArrayList<>();
	this.dbOriginal = DBMSConnection.getInstance();
	for( String tableName : dbOriginal.getAllTableNames() ){
	    Schema schema = dbOriginal.getSchema(tableName);
	    schemas.add(schema);
	}	
    }

    
    DatabaseModel createXmlModel(double scaleFactor){
	
	DatabaseModel result = new DatabaseModel();
	
	// Beans
	List<TableModel> tables = new ArrayList<>();
	List<SharingValues> sharing = new ArrayList<>();
	
	// Internal helpers
	TableModelMaker tMM = new TableModelMaker();
	
	for( Schema s : this.getSchemas() ){
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




    void printModelToFile(DatabaseModel model) throws IOException{
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
    
    private List<Schema> getSchemas(){
	return this.schemas;
    }
   
    class TableModelMaker{
	
	TableModel makeTableModel(
		Schema s) {
	    
	    TableModel result = new TableModel();
	    result.setName(s.getTableName());
	    
	    List<ColumnModel> cols = makeColumns(s);
	    result.setColumns(cols);
	    
	    return result;
	}
	
	private List<ColumnModel> makeColumns(Schema s) {
	    class ColumnModelMaker{
		    ColumnModel makeColumnModel(ColumnPumper<?> cP){
			ColumnModel result = new ColumnModel();
			
			// TODO : Case "not Fixed"
			result.setName(cP.getName());
			result.setDupsRatio(cP.getDuplicateRatio());
			result.setNullsRatio(cP.getNullRatio());
			return result;
		    }
	    };
	    
	    List<ColumnModel> result = new ArrayList<ColumnModel>();
	    
	    ColumnModelMaker cMM = new ColumnModelMaker();
	    for( ColumnPumper<?> cP : s.getColumns() ){
		result.add(cMM.makeColumnModel(cP));
	    }
	    
	    return result;
	}
    }
}

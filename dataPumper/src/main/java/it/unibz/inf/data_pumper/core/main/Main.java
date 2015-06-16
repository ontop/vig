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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import it.unibz.inf.data_pumper.configuration.Conf;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.core.statistics.xml_model.DatabaseModel;
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
    private static DatabaseModel dbModel;
    private static final String XML_MODEL_FILE_NAME = "src/main/resources/npd.xml";
    
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

	printModel();
    }

    private static void printModel() {
	// Mmmm
    }
    
    private static void jaxbObjectToXML(DatabaseModel data) {
	 
        try {
            JAXBContext context = JAXBContext.newInstance(DatabaseModel.class);
            Marshaller m = context.createMarshaller();
            //for pretty-print XML in JAXB
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 
            // Write to System.out for debugging
            // m.marshal(emp, System.out);
 
            // Write to File
            m.marshal(data, new File(XML_MODEL_FILE_NAME));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
};

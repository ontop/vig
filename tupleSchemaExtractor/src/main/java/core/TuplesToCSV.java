package core;

/*
 * #%L
 * tupleSchemasExtractor
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

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.parser.SQLQueryTranslator;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.api.VisitedQuery;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import utils.MyHashMapList;

public class TuplesToCSV {
	private String obdaFile = "resources/npd-v2-ql_a.obda";
	private String outCSVFile = null;
	private SQLQueryTranslator translator;
	private MyHashMapList<String, String> mFunct_Tuples;

	private static Logger logger = Logger.getLogger(TuplesToCSV.class.getCanonicalName());
	
	public TuplesToCSV(){}

	public TuplesToCSV(String obdaFile, String outCSVFile){
		this.obdaFile = obdaFile;
		this.outCSVFile = outCSVFile;
		this.mFunct_Tuples = new MyHashMapList<String, String>();
	}

	public void clusterizeFunctNames() throws Exception{
		/*
		 * Load the OBDA model from an external .obda file
		 */
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		OBDAModel obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdaFile);

		/* Open the csvFile in write mode */
		PrintWriter outCSV = new PrintWriter(new BufferedWriter(new FileWriter(outCSVFile)));

		Collection<OBDADataSource> sources = obdaModel.getSources();
		OBDADataSource source = sources.iterator().next();

		String url = source.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
		String username = source.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
		String password = source.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
		String driver = source.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);
		
		Set<String> noWhereNoJoin = new HashSet<String>();
		Set<String> joinAlso = new HashSet<String>();
		Set<String> joinOnly = new HashSet<String>();
		Set<String> whereOnlyNoJoin = new HashSet<String>();
		Set<String> whereNoJoin = new HashSet<String>();
		
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e1) {
			// Does nothing because the SQLException handles this problem also.
		}
		Connection localConnection = DriverManager.getConnection(url, username, password);

		// The SQL Translator
		translator = new SQLQueryTranslator(new DBMetadata(localConnection.getMetaData()));

		// Y shall do something similar for each dataproperty, and object property
		for( URI uri: obdaModel.getMappings().keySet() ){
			logger.info(uri);
			int greaterTwoCnt = 0;
			int twoCnt = 0;
			int cntNaryRules = 0;
			int cntNoJoinsNoWhere = 0; // 430 over 1000 and something
			for( OBDAMappingAxiom a : obdaModel.getMappings(uri) ){

				CQIE targetQuery = (CQIE) a.getTargetQuery();

				Function f = targetQuery.getBody().get(0);
				OBDASQLQuery sourceQuery = (OBDASQLQuery) a.getSourceQuery();

				// Construct the SQL query tree from the source query
				logger.info(sourceQuery.toString());
				VisitedQuery queryParsed = translator.constructParser(sourceQuery.toString());

				if( queryParsed.getJoinCondition().size() == 0 ){ // No joins
					if( queryParsed.getSelection() == null ){ // No where

						noWhereNoJoin.add(f.getFunctionSymbol().toString());
						whereNoJoin.add(f.getFunctionSymbol().toString());
					}
					else{
						if( queryParsed.getTableSet().size() == 1 ){
							// Where
							whereOnlyNoJoin.add(f.getFunctionSymbol().toString());
							whereNoJoin.add(f.getFunctionSymbol().toString());
						}
						else{
							// Join
							joinAlso.add(f.getFunctionSymbol().toString());
							joinOnly.add(f.getFunctionSymbol().toString());
						}
					}
				}
				else{
					// Join
					joinOnly.add(f.getFunctionSymbol().toString());
					joinAlso.add(f.getFunctionSymbol().toString());
				}
			}
			logger.debug("greater than two: "+ greaterTwoCnt); // 1028/
			logger.debug("two: "+twoCnt); // 0 ;)
			logger.debug("cntNaryRules "+cntNaryRules);
			logger.debug("cntNoJoinsNoWhere " + cntNoJoinsNoWhere);
			
			noWhereNoJoin.removeAll(whereOnlyNoJoin);
			noWhereNoJoin.removeAll(joinOnly);
			whereOnlyNoJoin.removeAll(joinOnly);
			whereNoJoin.removeAll(joinOnly);
			joinOnly.removeAll(noWhereNoJoin);
			joinOnly.removeAll(whereOnlyNoJoin);
			
			logger.debug("noWhereNoJoin");
			for( String s : noWhereNoJoin ){
				logger.debug(s);
			}
			logger.debug("whereOnlyNoJoin");
			for( String s : whereOnlyNoJoin ){
				logger.debug(s);
			}
			logger.debug("joinAlso");
			for( String s : joinAlso ){
				logger.debug(s);
			}
			logger.debug("joinOnly");
			for( String s : joinOnly ){
				logger.debug(s);
			}
			logger.debug("whereNoJoin");
			for( String s : whereNoJoin ){
				logger.debug(s);
			}
			// Write the HashMap to CSV
			outCSV.print(mFunct_Tuples.toCSV());

			outCSV.flush();
			outCSV.close();
		}	
	}
	
	public void play() throws Exception {
		/*
		 * Load the OBDA model from an external .obda file
		 */
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		OBDAModel obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdaFile);
		
		/* Open the csvFile in write mode */
		PrintWriter outCSV = new PrintWriter(new BufferedWriter(new FileWriter(outCSVFile)));
		
		Collection<OBDADataSource> sources = obdaModel.getSources();
		OBDADataSource source = sources.iterator().next();
		
		String url = source.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
		String username = source.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
		String password = source.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
		String driver = source.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e1) {
			// Does nothing because the SQLException handles this problem also.
		}
		Connection localConnection = DriverManager.getConnection(url, username, password);
		
		// The SQL Translator
		translator = new SQLQueryTranslator(new DBMetadata(localConnection.getMetaData()));
		
		Set<String> whereOrJoin = new HashSet<String>();
		
		// Y shall do something similar for each dataproperty, and object property
		for( URI uri: obdaModel.getMappings().keySet() ){
			logger.debug(uri);
			int greaterTwoCnt = 0;
			int twoCnt = 0;
			int cntNaryRules = 0;
			int cntNoJoinsNoWhere = 0; // 430 over 1000 and something
			for( OBDAMappingAxiom a : obdaModel.getMappings(uri) ){
				
				CQIE targetQuery = (CQIE) a.getTargetQuery();
				
				if( targetQuery.getBody().size() > 1 ){
					++cntNaryRules;
				} // ZERO. So, every mapping assertion models EXACTLY one thing
				
				boolean isRoleOrData = false;
				// For each atom in the body, get the arity
				for( Function f : targetQuery.getBody() ){
					logger.debug(f.getFunctionSymbol());
					if( f.getArity() == 2 ){ // role or data property
						++twoCnt;
						isRoleOrData = true;
					}
					if( f.getArity() > 2 ){
						++greaterTwoCnt;
					}
				}
				
				Function f = targetQuery.getBody().get(0);
				OBDASQLQuery sourceQuery = (OBDASQLQuery) a.getSourceQuery();
				
				// Construct the SQL query tree from the source query
				logger.debug(sourceQuery.toString());
				VisitedQuery queryParsed = translator.constructParser(sourceQuery.toString());
				
				if( isRoleOrData && queryParsed.getJoinCondition().size() == 0 ){ // No explicit join 
					if( queryParsed.getSelection() == null ){ // No where. But there might be joins
						if( queryParsed.getTableSet().size() > 1 ){ // Implicit join
							whereOrJoin.add(f.getFunctionSymbol().toString());
						}
						else{
							if( !queryParsed.getProjection().getColumnNameList().isEmpty() ){
								++cntNoJoinsNoWhere; // 430
								
								// tableName projectionList
								// If the tableSet is greater than one, then there is some problem
								if( queryParsed.getTableSet().size() > 1 ){
								logger.debug("Table Set for the query > 1");
								System.exit(1);
								}
								StringBuilder builder = new StringBuilder();
								
								builder.append(queryParsed.getTableSet().get(0)); // TableName
								builder.append(" "); // CSV separator
								
								String postfix = parseBinaryFunct(f);
								
								if(postfix != null)
									builder.append(postfix);
								else continue; // < 2 variables
								
								mFunct_Tuples.put(f.getFunctionSymbol().toString(), builder.toString());
							}
						}
					}
					else{
						whereOrJoin.add(f.getFunctionSymbol().toString());
					}
				}
				else whereOrJoin.add(f.getFunctionSymbol().toString());
			}
			logger.debug("greater than two: "+ greaterTwoCnt); // 0
			logger.debug("two: "+twoCnt); // 1028 ;)
			logger.debug("cntNaryRules "+cntNaryRules);
			logger.debug("cntMaybeNoJoinsNoWhere " + cntNoJoinsNoWhere);
			
			mFunct_Tuples.removeAll(whereOrJoin);
			
			logger.debug("cntNoJoinsNoWhere " + mFunct_Tuples.keyset().size());
			
			// Write the HashMap to CSV
			outCSV.print(mFunct_Tuples.toCSV());
			
			outCSV.flush();
			outCSV.close();
		}	
	}
	
	/**
	 * <i>uri1Template uri2Template fillerCol1 fillerCol2 fillerCol3 ... fillerColn</i>
	 * @return
	 */
	public String parseBinaryFunct(Function f){
		
		String[] uriTemplates = new String[2];
		Set<String> vars = new HashSet<String>();
		
		if(f.getArity() != 2){
			return null;
		}
		
		for( int i = 0; i < 2; ++i ){
			if( f.getTerm(i).toString().contains("URI(") ){
				
				uriTemplates[i] = cleanURIFromVariable(null, f.getTerm(i).toString());
				for( Variable v : f.getTerm(i).getReferencedVariables() ){					
					vars.add(v.toString());
				}
			}
			else{
				String v = f.getTerm(i).getReferencedVariables().iterator().next().toString();
				logger.debug(f);
				uriTemplates[i] = cleanURIFromVariable(v, f.getTerm(i).toString());
				vars.add(v);
			}
		}
		if( vars.size() < 2 ) return null;
		StringBuilder builder = new StringBuilder();
		
		builder.append(uriTemplates[0]+ " ");
		builder.append(uriTemplates[1]+ " ");
		
		int i = 0;
		for( String v : vars ){
			builder.append(v);
			if( ++i < vars.size() ){
				builder.append(" ");
			}
		}
		return builder.toString();
	}
	
	public String cleanURIFromVariable(String var, String uri){
		// URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/core/{}",wlbNpdidWellbore,wlbCoreNumber), 
		// http://www.w3.org/2001/XMLSchema#decimal(wlbCoreIntervalBottomFT)
		String result = null;
		
		if( !uri.startsWith("URI(") ){
			
			if( !uri.contains("(") ){
				if( var.equals(uri) ){ 
					return "-";
				}
			}
			int begin = uri.indexOf("(") + 1;
			int end = uri.indexOf(")");
			String candidate = uri.substring(begin, end);
			if( var.equals(candidate) ){ 
				String prefix = uri.substring(0, begin);
				String postfix = uri.substring(end);
				result = prefix + postfix;
			}
		}
		else{
			int begin = uri.indexOf("\"") + 1;
			int end = uri.lastIndexOf("\"");
			result = uri.substring(begin, end);
		}
		return result;
	}
	
	public static void main(String[] args){
		TuplesToCSV a = new TuplesToCSV("resources/npd-v2-ql_a.obda", "resources/outCSV");
		
		try {
//			a.clusterizeFunctNames();
			a.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
};

package it.unibz.inf.vig_mappings_analyzer.core;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.parser.SQLQueryParser;

import org.apache.log4j.Logger;

public abstract class OntopConnection {
    // Parameters
    protected String obdaFile = "resources/npd-v2-ql_a.obda";

    // Internal State
    protected SQLQueryParser translator;
    protected OBDAModel obdaModel;

    protected static Logger logger = Logger.getLogger(JoinableColumnsFinder.class.getCanonicalName());

    protected OntopConnection(OBDAModel obdaModel, SQLQueryParser parser) {
	this.obdaModel = obdaModel;
	this.translator = parser;
    }
}

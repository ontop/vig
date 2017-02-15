package it.unibz.inf.vig_mappings_analyzer.core;

import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.sql.DBMetadata;

import org.apache.log4j.Logger;

public abstract class OntopConnection {
    // Parameters
    protected String obdaFile = "resources/npd-v2-ql_a.obda";

    // Internal State
    protected DBMetadata meta;
    protected OBDAModel obdaModel;

    protected static Logger logger = Logger.getLogger(JoinableColumnsFinder.class.getCanonicalName());

    protected OntopConnection(OBDAModel obdaModel, DBMetadata meta) {
	this.obdaModel = obdaModel;
	this.meta = meta;
    }
}

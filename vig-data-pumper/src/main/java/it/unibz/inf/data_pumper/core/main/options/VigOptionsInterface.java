package it.unibz.inf.data_pumper.core.main.options;

import it.unibz.inf.vig_options.core.BooleanOption;
import it.unibz.inf.vig_options.core.DoubleOption;
import it.unibz.inf.vig_options.core.IntOption;
import it.unibz.inf.vig_options.core.StringOption;
import it.unibz.inf.vig_options.core.StringOptionWithRange;
import it.unibz.inf.vig_options.ranges.DoubleRange;
import it.unibz.inf.vig_options.ranges.IntRange;
import it.unibz.inf.vig_options.ranges.StringRange;

public abstract class VigOptionsInterface {
    // Options
    protected static final DoubleOption optScaling = new DoubleOption("--scale", "It specifies the scaling factor", "PUMPER", 1, new DoubleRange(0, Double.MAX_VALUE, false, true));	
    protected static final StringOption optResources = new StringOption("--res", "Location of the resources directory", "CONFIGURATION", "resources");
    protected static final StringOption optConfig = new StringOption("--conf", "Name of the configuration file", "CONFIGURATION", "resources/configuration.conf");
    protected static final StringOption optTables = new StringOption("--tables", "Restrict the generation to a list of tables. E.g., --tables=table1,table2,table3,etc.", "PUMPER", "");
    protected static final StringOption optColumns = new StringOption("--columns", "Restrict the generation to a list of columns. E.g., --columns=table1.col1,table2.col2,etc.", "PUMPER", "");

    // DB Connection
    protected static final StringOption optJdbcConnector = new StringOption("--jdbc", "Database Driver Class", "PUMPER", "");
    protected static final StringOption optDbUrl = new StringOption("--db-url", "Database URL.", "PUMPER", "");
    protected static final StringOption optDbUser = new StringOption("--db-user", "Username for accessing the database", "PUMPER", "");
    protected static final StringOption optDbPwd = new StringOption("--db-pwd", "Password for accessing the database", "PMPER", "");

    // Mode selection
    protected static final BooleanOption optRandomGen = new BooleanOption("--random-gen", "Ignore database statistics, and generate with random statistics.", "PUMPER", false);
    protected static final StringOption optMappingsFile = new StringOption("--mappings", "Path to the Mappings File. This parameter is mandatory in OBDA-mode", "PUMPER", "");
    protected static final StringOptionWithRange optMode = new StringOptionWithRange("--mode", "The mode of generation. "
	    + "One of: Database statistics only mode (DB), or Database statistics and Mappings analysis mode (OBDA).", "PUMPER", "OBDA", new StringRange("[DB,OBDA]"));
    protected static final IntOption optCCAnalysisTimeout = new IntOption("--ccAnalysisTimeout", "Timeout allowed to the columns-cluster analysis, in seconds. "
	    + "To be used in combination with `OBDA` mode.", "PUMPER", 60, new IntRange(0, Integer.MAX_VALUE, true, true));    
    protected static final StringOption optFixedList = new StringOption("--fixed", "Space-separated list of fully qualified column names (e.g., tableName.colName) declared as `fixed domain`, "
	    + "that is, attribute names for which "
	    + "no values should be generated apart from those that can be found in the seed database instance.", "PUMPER", "");
    protected static final StringOption optNonFixedList = new StringOption("--non-fixed", "Space-separated list of qualified attribute names (e.g., tableName.colName) for which "
	    + "fresh values should be generated according to the statistics analysis. Useful in case VIG sets some column as ", "PUMPER", "");
}

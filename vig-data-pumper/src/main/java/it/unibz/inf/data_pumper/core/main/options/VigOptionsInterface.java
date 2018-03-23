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

    // Configuration
    protected static final StringOption optRes =
            new StringOption("--res", "Resources Folder", "Mandatory", "resources");

    // Options
    protected static final DoubleOption optScaling =
            new DoubleOption("--scale", "It specifies the scaling factor",
                    "Mandatory", 1, new DoubleRange(0, Double.MAX_VALUE, false, true));
    protected static final StringOption optTables =
            new StringOption("--tables",
                    "Restrict the generation to a list of tables. " +
                            "E.g., --tables=table1,table2,table3,etc.", "Advanced", "");
    protected static final StringOption optColumns =
            new StringOption("--columns",
                    "Restrict the generation to a list of columns. " +
                            "E.g., --columns=table1.col1,table2.col2,etc.", "Advanced", "");

    // DB Connection
    protected static final StringOption optJdbcConnector =
            new StringOption("--jdbc", "Database Connector", "Mandatory", "jdbc:mysql");
    protected static final StringOption optDbUrl =
            new StringOption("--db-url", "Database URL.", "Mandatory", "");
    protected static final StringOption optDbUser =
            new StringOption("--db-user", "Username for accessing the database", "Mandatory", "");
    protected static final StringOption optDbPwd =
            new StringOption("--db-pwd", "Password for accessing the database", "Mandatory", "");

    // Mode selection
    protected static final BooleanOption optRandomGen =
            new BooleanOption("--random-gen",
                    "Ignore database statistics, and generate " +
                            "with random statistics.", "Advanced", false);
    protected static final StringOption optMappingsFile =
            new StringOption("--mappings",
                    "Path to the Mappings File. This parameter " +
                            "is mandatory in OBDA-mode", "Mandatory", "");
    protected static final StringOptionWithRange optMode =
            new StringOptionWithRange("--mode", "The mode of generation. "
                    + "One of: Database statistics only mode (DB), or Database " +
                    "statistics and Mappings analysis mode (OBDA).",
                    "Mandatory", "OBDA", new StringRange("[DB,OBDA]"));
    protected static final IntOption optCCAnalysisTimeout =
            new IntOption("--cc-timeout",
                    "Timeout allowed to the columns-cluster analysis, in seconds. "
                            + "Meaningful only in combination with `OBDA` mode.",
                    "Advanced", 60, new IntRange(0, Integer.MAX_VALUE, true, true));
    protected static final StringOption optFixedList =
            new StringOption("--fixed", "Space-separated list " +
                    "of fully qualified column names (e.g., tableName.colName) declared as `fixed domain`, "
                    + "that is, attribute names for which "
                    + "no values should be generated apart from " +
                    "those that can be found in the source database instance.", "Advanced", "");
  protected static final StringOption optNonFixedList =
          new StringOption("--non-fixed",
                  "Space-separated list of qualified attribute" +
                          " names (e.g., tableName.colName) for which "
                          + "fresh values should be generated according " +
                          "to the statistics analysis. Useful in case" +
                          " VIG sets some column as ", "Advanced", "");
}

Parallelization Support
---

VIG provides a command line parameter that restricts the data to be generated to a granularity up to the level of a single column in the database.

~~~
--tables            <string>                                               (default: )         Restrict the generation to a list of tables. E.g., --tables=table1,table2,table3,etc.

--columns           <string>                                               (default: )         Restrict the generation to a list of columns. E.g., --columns=table1.col1,table2.col2,etc.
~~~

Assume you have 10 machines available, and you want to generate 10 tables (table1, ..., table10). Then you can install a copy of VIG in each machine, and launch for the machine n the following command:

~~~
java -jar vig-version.jar --tables=tablen
~~~

The above command will force the machine `n` to only produce data for the table `tablen`. The produced CSV files can then be imported in the target RDBMS in the usual way.

The procedure with the parameter `--columns` is the same, with the only difference that the CSV files generated for each column must then be merged into a single CSV file that respects the order of the columns in the table. In UNIX systems, this is usually done through the [`paste`](https://en.wikipedia.org/wiki/Paste_(Unix)) command. Future releases of VIG will provide a command line utility that achieves the same result in an automatized way. 

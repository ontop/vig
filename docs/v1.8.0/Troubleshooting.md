# Troubleshooting

A list of common issues and possible solutions:

- [NullPointerException](#nullpointerexception)
- [Connection refused](#connection-refused)
- [Operation timed out](#operation-timed-out)
- [FileNotFoundException src/main/resources/csvs/*.csv](#filenotfoundexception-src-main-resources-csvs--csv)

### NullPointerException

```
- MESSAGE: null
	...
Caused by: java.lang.NullPointerException
	... 
com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException: No operations allowed after connection closed.
        ...
Exception in thread "main" java.lang.NullPointerException
	at it.unibz.inf.data_pumper.core.main.DatabasePumperOBDA.establishColumnBounds(DatabasePumperOBDA.java:96)
	at it.unibz.inf.data_pumper.core.main.DatabasePumperDB.pumpDatabase(DatabasePumperDB.java:81)
	at it.unibz.inf.data_pumper.core.main.Main.main(Main.java:107)
```

The connection could not be established, check that you modified the information about the JDBC connection in your configuration file (default configuration file: src/main/resources/configuration.conf) .

### Connection refused

```
com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure

The last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server.
	...
Caused by: java.net.ConnectException: Connection refused
	... 
Exception in thread "main" java.lang.NullPointerException
	...
```

The connection could not be established, check that you modified the information about the JDBC connection in your configuration file  (default configuration file: src/main/resources/configuration.conf) 

### Operation timed out

```
com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure

The last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server.
	...
Caused by: java.net.ConnectException: Operation timed out
	....
com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException: No operations allowed after connection closed.
	...
Exception in thread "main" java.lang.NullPointerException
	at it.unibz.inf.data_pumper.core.main.DatabasePumperOBDA.establishColumnBounds(DatabasePumperOBDA.java:96)
	at it.unibz.inf.data_pumper.core.main.DatabasePumperDB.pumpDatabase(DatabasePumperDB.java:81)
	at it.unibz.inf.data_pumper.core.main.Main.main(Main.java:107)
```

If pumper-type is ODBA: The timeout can be caused by missing or wrong information, check that your obda file in the SourceDeclaration contains the same JDBC connection information as you defined in your configuration file (default configuration file: src/main/resources/configuration.conf)  

###  FileNotFoundException src/main/resources/csvs/*.csv 

```
java.io.FileNotFoundException: src/main/resources/csvs/address.csv (No such file or directory)
	...
	at it.unibz.inf.data_pumper.core.main.DatabasePumperDB.generateForTable(DatabasePumperDB.java:138)
	at it.unibz.inf.data_pumper.core.main.DatabasePumperDB.generate(DatabasePumperDB.java:189)
	at it.unibz.inf.data_pumper.core.main.DatabasePumperDB.pumpDatabase(DatabasePumperDB.java:98)
	at it.unibz.inf.data_pumper.core.main.Main.main(Main.java:107)
```
The directory csvs is missing. Create the directory where the csv data will be generated (default location: src/main/resources/csvs).

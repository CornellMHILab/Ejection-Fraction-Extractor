package readers

import gov.va.vinci.leo.cr.BatchDatabaseCollectionReader;

// String driver = "com.mysql.jdbc.Driver"
// String url = "jdbc:mysql://localhost/<database_name>"
// String username="<username>";
// String password="<password>";
// String query = "SELECT ID, TEXT FROM <database_name>.<schema>.<table_name>"

String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
String url = "jdbc:sqlserver://<database_engine>:1433;databasename=<database_name>;integratedSecurity=false"
String username="<username>";
String password = "<password>"
String query = "SELECT  DocID, DocText   FROM <database_name>.<table_name> order by DocID"

int startingIndex = 0;
int endingIndex = 10;
int batch_size = 10000;

reader = new BatchDatabaseCollectionReader(
        driver,
        url,
        username,
        password,
        query,
        "id","text",   /// Make sure that the names of the fields are low case.
        startingIndex , endingIndex
        , batch_size)
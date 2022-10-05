package readers

import gov.va.vinci.leo.cr.BatchDatabaseCollectionReader;

String driver = "com.mysql.jdbc.Driver"
String url = "jdbc:mysql://<server>/<db>"
String username="<usr>";
String password="<paswd>";
String query = "<query>"



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

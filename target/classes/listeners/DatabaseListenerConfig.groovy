package listeners

import gov.va.vinci.ef.listeners.BasicDatabaseListener

int batchSize = 1000

// String url = "jdbc:mysql://localhost:3306/clarityhf?autoReconnect=true&useSSL=false"
// String driver = "com.mysql.jdbc.Driver"
// String dbUser = "clarityhf"
// String dbPwd = "clarityhf"
// String dbsName = "clarityhf"
// String tableName = "lvef_output_test"
// incomingTypes = "gov.va.vinci.ef.types.Relation"
// // incomingTypes = "gov.va.vinci.ef.types.MeasurementWindow"


String url = "jdbc:sqlserver://vits-archsqlp03.a.wcmc-ad.net:1433;databasename=SUPER_NLP;integratedSecurity=false"
String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
String dbUser = "usr_dm_nlp"
String dbPwd = "us3r_83_Nlp"
String dbsName = "SUPER_NLP"
String tableName = "dbo.EFEX_LEO_OUTPUT"
incomingTypes = "gov.va.vinci.ef.types.Relation"

fieldList = [
        ["DocId", "0", "varchar(500)"],
        ["Term", "-1", "varchar(500)"],
        ["Value", "-1", "varchar(100)"],
        ["Value2", "-1", "varchar(100)"],
        ["ValueString", "-1", "varchar(100)"],
        ["InstanceID", "-1", "int"],
        ["Snippets", "-1", "varchar(8000)"],
        ["SpanStart", "-1", "int"],
        ["SpanEnd", "-1", "int"],
        ["RunDate", "-1", "date"]
]

boolean dropExisting = false;
listener = BasicDatabaseListener.createNewListener(
        driver,
        url,
        dbUser,
        dbPwd,
        dbsName,
        tableName,
        batchSize,
        fieldList,
        incomingTypes)

// Comment out the statement below if you want to add to the existing table
listener.createTable(dropExisting);

package readers

import gov.va.vinci.leo.cr.BatchDatabaseCollectionReader;

// String driver = "com.mysql.jdbc.Driver"
// String url = "jdbc:mysql://localhost/clarityhf"
// String username="clarityhf";
// String password="clarityhf";
// String query = "SELECT ID, TEXT FROM clarityhf.echo_reports limit 20"
// String query = "SELECT ID, TEXT FROM clarityhf.echo_reports"

String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
String url = "jdbc:sqlserver://vits-archsqlp03.a.wcmc-ad.net:1433;databasename=SUPER_NLP;integratedSecurity=false"
String username="usr_dm_nlp";
String password = "us3r_83_Nlp"
// String query = "SELECT CONCAT(ORDER_PROC_ID, '|', PAT_ID, '|', COMPONENT_ID, '|', CONVERT(VARCHAR,ORDERING_DATE,121) ) AS ID, RESULTS_COMP_CMT AS TEXT FROM DM_NLP.dbo.TEST_LEO_REPORTS WHERE ORDER_PROC_ID = 1394704"
// String query = "SELECT CONCAT(ORDER_PROC_ID, '|', PAT_ID, '|', COMPONENT_ID, '|', CONVERT(VARCHAR,ORDERING_DATE,121) ) AS ID, RESULTS_COMP_CMT AS TEXT FROM DM_NLP.dbo.TEST_LEO_REPORTS"
// String query = "SELECT ORDER_PROC_ID AS ID, RESULTS_COMP_CMT AS TEXT FROM DM_NLP.dbo.TEST_LEO_REPORTS"
// String query = "SELECT PAT_ID, NOTE_TEXT FROM DM_NLP.dbo.PHQ_TRAIN ORDER BY PAT_ID DESC, NOTE_ID DESC OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY"
String query = "SELECT TOP 1000 CONCAT(ORDER_PROC_ID, '|', PAT_ID, '|', COMPONENT_ID, '|', CONVERT(VARCHAR,ORDERING_DATE,121) ) AS ID, RESULTS_COMP_CMT AS TEXT FROM SUPER_NLP.dbo.ECHO_REPORTS_VIEW"
// String query = "SELECT CONCAT(ORDER_PROC_ID, '|', PAT_ID, '|', COMPONENT_ID, '|', CONVERT(VARCHAR,ORDERING_DATE,121) ) AS ID, RESULTS_COMP_CMT AS TEXT FROM SUPER_NLP.dbo.ECHO_REPORTS_VIEW ) tab WHERE rowNum between 0 and 100000"
// String query = "SELECT CONCAT(ORDER_ID, '|', PAT_ID, '|', COMPONENT_ID, '|', CONVERT(VARCHAR, ORDERING_DATE,121) ) AS ID, RESULTS_COMP_CMT AS TEXT FROM SUPER_NLP.dbo.ECHO_REPORTS_VIEW  WHERE convert(date, ETL_DATE) > (SELECT max(RunDate) from SUPER_NLP.dbo.EFEX_LEO_OUTPUT)"



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
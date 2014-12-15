package com.nuodb.migrator.jdbc.url;

import static com.nuodb.migrator.jdbc.url.JdbcUrlConstants.DB2_SUB_PROTOCOL;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class DB2JdbcUrl extends JdbcUrlBase {
    
    public static JdbcUrlParser getParser() {
        return new JdbcUrlParserBase(DB2_SUB_PROTOCOL) {
            @Override
            protected JdbcUrl createJdbcUrl(String url) {
                return new DB2JdbcUrl(url);
            }
        };
    }

    protected DB2JdbcUrl(String url) {
        super(url, DB2_SUB_PROTOCOL);
    }

    @Override
    protected void parseSubName(String subName) {
        if(!isEmpty(substringAfterLast(subName, ":")) && substringAfterLast(subName, ":").indexOf("/") < 0){
            parseParameters(getParameters(), substringAfterLast(subName, ":"), ";");
        }
    }
    
    public String getQualifier() {
        return null;
    }

    @Override
    public String getCatalog() {
        return null;
    }

    @Override
    public String getSchema() {
        return null;
    }
}
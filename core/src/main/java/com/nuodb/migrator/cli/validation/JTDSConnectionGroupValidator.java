package com.nuodb.migrator.cli.validation;

import static com.nuodb.migrator.jdbc.JdbcConstants.JTDS_SQLSERVER_DRIVER;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.nuodb.migrator.cli.parse.CommandLine;
import com.nuodb.migrator.cli.parse.Option;

public class  JTDSConnectionGroupValidator extends ConnectionGroupValidator {

    private transient final Logger logger = getLogger(getClass());

    public JTDSConnectionGroupValidator(ConnectionGroupInfo connectionGroupInfo) {
        super(connectionGroupInfo);
    }

    @Override
    public boolean canValidate(CommandLine commandLine, Option option) {
        return StringUtils.equals(getDriverValue(commandLine), JTDS_SQLSERVER_DRIVER);
    }

    @Override
    protected void dbUserWarnMessage(String jdbcUsername, String jdbcPassword, String optionUsername, String optionPasssword) {
        if(!StringUtils.equals(optionUsername, jdbcUsername) || !StringUtils.equals(optionPasssword, jdbcPassword)){
            logger.warn(format("JDBC URL parameters user: %s passowrd: %s are not matching with commandline options --source.username %s --source.password %s.",jdbcUsername, jdbcPassword, optionUsername,optionPasssword));
            logger.warn(format("JDBC URL parameters user: %s password: %s are used for database connection", jdbcUsername, jdbcPassword));
        }
    }
}
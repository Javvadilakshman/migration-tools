package com.nuodb.migrator.cli.validation;

import static com.nuodb.migrator.jdbc.JdbcConstants.MS_SQLSERVER_DRIVER;
import static org.slf4j.LoggerFactory.getLogger;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.nuodb.migrator.cli.parse.CommandLine;
import com.nuodb.migrator.cli.parse.Option;

public class MSSQLConnectionGroupValidator extends ConnectionGroupValidator {

    private transient final Logger logger = getLogger(getClass());
    
    public MSSQLConnectionGroupValidator(ConnectionGroupInfo connectionGroupInfo) {
        super(connectionGroupInfo);
    }

    @Override
    public boolean canValidate(CommandLine commandLine, Option option) {
        return StringUtils.equals(getDriverValue(commandLine), MS_SQLSERVER_DRIVER);
    }

}
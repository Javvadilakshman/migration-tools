/**
 * Copyright (c) 2014, NuoDB, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of NuoDB, Inc. nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NUODB, INC. BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.nuodb.migrator.cli.validation;

import static com.nuodb.migrator.jdbc.url.JdbcUrlConstants.USER;
import static com.nuodb.migrator.jdbc.url.JdbcUrlConstants.PASSWORD;
import static com.nuodb.migrator.jdbc.url.JdbcUrlParsers.getInstance;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.nuodb.migrator.cli.parse.CommandLine;
import com.nuodb.migrator.cli.parse.Option;
import com.nuodb.migrator.cli.parse.OptionValidator;
import com.nuodb.migrator.jdbc.url.JdbcUrl;

/**
 * @author Sergey Bushik
 */
public abstract class ConnectionGroupValidator implements OptionValidator {
    
    private transient final Logger logger = getLogger(getClass());
    
    private ConnectionGroupInfo connectionGroupInfo;

    public ConnectionGroupValidator(ConnectionGroupInfo connectionGroupInfo) {
        this.connectionGroupInfo = connectionGroupInfo;
    }

    public String getDriverValue(CommandLine commandLine) {
        return getOptionValue(commandLine, connectionGroupInfo.getDriverOption());
    }

    public String getUrlValue(CommandLine commandLine) {
        return getOptionValue(commandLine, connectionGroupInfo.getUrlOption());
    }

    public String getUsernameValue(CommandLine commandLine) {
        return getOptionValue(commandLine, connectionGroupInfo.getUsernameOption());
    }

    public String getPasswordValue(CommandLine commandLine) {
        return getOptionValue(commandLine, connectionGroupInfo.getPasswordOption());
    }

    public String getCatalogValue(CommandLine commandLine) {
        return getOptionValue(commandLine, connectionGroupInfo.getCatalogOption());
    }

    public String getSchemaValue(CommandLine commandLine) {
        return getOptionValue(commandLine, connectionGroupInfo.getSchemaOption());
    }

    public String getPropertiesValue(CommandLine commandLine) {
        return getOptionValue(commandLine, connectionGroupInfo.getPropertiesOption());
    }

    public String getOptionValue(CommandLine commandLine, String option) {
        return option != null ? (String) commandLine.getValue(option) : null;
    }

    public String getDriverOption() {
        return connectionGroupInfo.getDriverOption();
    }

    public String getUrlOption() {
        return connectionGroupInfo.getUrlOption();
    }

    public String getCatalogOption() {
        return connectionGroupInfo.getCatalogOption();
    }

    public String getSchemaOption() {
        return connectionGroupInfo.getSchemaOption();
    }

    public String getUsernameOption() {
        return connectionGroupInfo.getUsernameOption();
    }

    public String getPropertiesOption() {
        return connectionGroupInfo.getPropertiesOption();
    }

    public String getPasswordOption() {
        return connectionGroupInfo.getPasswordOption();
    }

    @Override
    public void validate(CommandLine commandLine, Option option) {
        
        JdbcUrl jdbcUrl = getInstance().parse(getUrlValue(commandLine));
        if(jdbcUrl==null) return;
        
        String jdbcUsername = (String)jdbcUrl.getParameters().get(USER);
        String jdbcPassword = (String)jdbcUrl.getParameters().get(PASSWORD);

        if(StringUtils.isBlank(jdbcUsername) && StringUtils.isBlank(jdbcPassword)) 
            return;
        
        String optionUsername = getUsernameValue(commandLine);
        String optionPasssword = getPasswordValue(commandLine);
        
        if(StringUtils.isBlank(optionUsername) && StringUtils.isBlank(optionPasssword)) 
            return;
        
        dbUserWarnMessage(jdbcUsername, jdbcPassword, optionUsername, optionPasssword);
    }
    
    protected void dbUserWarnMessage(String jdbcUsername, String jdbcPassword, String optionUsername, String optionPasssword) {
        if(!StringUtils.equals(optionUsername, jdbcUsername) || !StringUtils.equals(optionPasssword, jdbcPassword)){
            logger.warn(format("JDBC URL parameters user: %s passowrd: %s are not matching with commandline options --source.username %s --source.password %s.",jdbcUsername, jdbcPassword, optionUsername,optionPasssword));
            logger.warn(format("Commandline option values --source.username %s --source.password %s are used for database connection.", optionUsername, optionPasssword));
        }
        
    }
}

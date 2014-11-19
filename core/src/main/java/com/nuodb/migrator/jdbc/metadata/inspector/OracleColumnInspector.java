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
package com.nuodb.migrator.jdbc.metadata.inspector;

import com.nuodb.migrator.jdbc.metadata.Column;
import com.nuodb.migrator.jdbc.query.SelectQuery;
import com.nuodb.migrator.jdbc.query.StatementAction;
import com.nuodb.migrator.jdbc.query.StatementFactory;
import com.nuodb.migrator.jdbc.query.StatementTemplate;
import com.nuodb.migrator.jdbc.type.JdbcTypeDesc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static com.nuodb.migrator.jdbc.metadata.DefaultValue.valueOf;
import static com.nuodb.migrator.jdbc.model.FieldFactory.newFieldList;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Fixes "Stream has already been closed" https://issues.apache.org/jira/browse/DDLUTILS-29
 *
 * @author Sergey Bushik
 */
public class OracleColumnInspector extends SimpleColumnInspector {

    /**
     * Fetches LONG or LONG RAW columns first, as these kind of columns are read as stream, if not read in a proper
     * order, there will be an error
     *
     * @param inspectionContext with inspection data
     * @param columns       result set holding column attributes
     * @param column        to populate from result set
     * @throws SQLException
     */
    @Override
    protected void processColumn(InspectionContext inspectionContext, ResultSet columns, Column column) throws SQLException {
        String defaultValue = trim(columns.getString("COLUMN_DEF"));
        if (startsWith(defaultValue, "'") && endsWith(defaultValue, "'")) {
            defaultValue = defaultValue.substring(1, defaultValue.length() - 1);
        }
        JdbcTypeDesc typeDescAlias = inspectionContext.getDialect().getJdbcTypeAlias(
                columns.getInt("DATA_TYPE"), columns.getString("TYPE_NAME"));
        column.setTypeCode(typeDescAlias.getTypeCode());
        column.setTypeName(typeDescAlias.getTypeName());

        int columnSize = columns.getInt("COLUMN_SIZE");
        column.setSize(columnSize);
        column.setPrecision(columnSize);
        column.setScale(columns.getInt("DECIMAL_DIGITS"));

        column.setComment(columns.getString("REMARKS"));
        column.setPosition(columns.getInt("ORDINAL_POSITION"));
        String autoIncrement =
                newFieldList(columns.getMetaData()).get("IS_AUTOINCREMENT") != null ?
                        columns.getString("IS_AUTOINCREMENT") : null;
        column.setAutoIncrement("YES".equals(autoIncrement));
        column.setNullable("YES".equals(columns.getString("IS_NULLABLE")));
        column.setDefaultValue(valueOf(defaultValue, true));
        column.setUserDefinedType(userDefinedTypes != null ? userDefinedTypes.contains(column.getTypeName()):false);
    }

    /**
     * Fetches all the user types for a schema
     *
     * @param inspectionContext with inspection data
     * @param schema   Schema value
     * @throws SQLException
     */
    @Override
    protected ArrayList<String> getUserDefinedTypes(InspectionContext inspectionContext, final String schema) 
            throws SQLException {
            StatementTemplate template = new StatementTemplate(inspectionContext.getConnection());
            ArrayList<String> udTypeList = template.executeStatement(
                    new StatementFactory<PreparedStatement>() {
                        @Override
                        public PreparedStatement createStatement(Connection connection) throws SQLException {
                            SelectQuery uerTypeQuery = new SelectQuery();
                            uerTypeQuery.column("TYPE_NAME");
                            uerTypeQuery.from("ALL_TYPES");
                            uerTypeQuery.where("OWNER = ?");
                            return connection.prepareStatement(uerTypeQuery.toString());
                        }
                    }, new StatementAction<PreparedStatement, ArrayList<String>>() {
                        @Override
                        public ArrayList<String> executeStatement(PreparedStatement statement) throws SQLException {
                            statement.setString(1, schema);
                            ResultSet udTypesRS = statement.executeQuery();
                            ArrayList<String> udTypes = new ArrayList<String>();
                            while (udTypesRS.next()) {
                                udTypes.add(udTypesRS.getString(1));
                            }
                            return udTypes;
                        }
                    }
            );
        return udTypeList;
    }
}

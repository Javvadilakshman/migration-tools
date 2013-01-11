/**
 * Copyright (c) 2012, NuoDB, Inc.
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
package com.nuodb.migration.jdbc.metadata.inspector;

import com.google.common.collect.Maps;
import com.nuodb.migration.jdbc.metadata.Database;
import com.nuodb.migration.jdbc.metadata.Identifier;
import com.nuodb.migration.jdbc.metadata.MetaDataType;
import com.nuodb.migration.jdbc.metadata.Table;
import com.nuodb.migration.jdbc.query.StatementCallback;
import com.nuodb.migration.jdbc.query.StatementCreator;
import com.nuodb.migration.jdbc.query.StatementTemplate;

import java.sql.*;
import java.util.Map;

import static com.nuodb.migration.jdbc.JdbcUtils.close;

/**
 * @author Sergey Bushik
 */
public class PostgreSQLCheckConstraintReader extends MetaDataReaderBase {

    public static final String QUERY =
            "SELECT *\n" +
            "FROM INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE AS CCU\n" +
            "INNER JOIN INFORMATION_SCHEMA.CHECK_CONSTRAINTS AS CC ON\n" +
            "CCU.TABLE_CATALOG = CC.CONSTRAINT_CATALOG\n" +
            "AND CCU.TABLE_SCHEMA = CC.CONSTRAINT_SCHEMA\n" +
            "AND CCU.CONSTRAINT_NAME = CC.CONSTRAINT_NAME\n" +
            "WHERE CCU.TABLE_SCHEMA = ? AND CCU.TABLE_NAME = ?";

    public PostgreSQLCheckConstraintReader() {
        super(MetaDataType.CHECK_CONSTRAINT);
    }

    @Override
    public void read(DatabaseInspector inspector, final Database database,
                     DatabaseMetaData databaseMetaData) throws SQLException {
        StatementTemplate template = new StatementTemplate(databaseMetaData.getConnection());
        template.execute(
                new StatementCreator<PreparedStatement>() {
                    @Override
                    public PreparedStatement create(Connection connection) throws SQLException {
                        return connection.prepareStatement(QUERY);
                    }
                },
                new StatementCallback<PreparedStatement>() {

                    @Override
                    public void execute(PreparedStatement statement) throws SQLException {
                        for (Table table : database.listTables()) {
                            statement.setString(1, table.getSchema().getName());
                            statement.setString(2, table.getName());
                            ResultSet resultSet = statement.executeQuery();
                            try {
                                read(table, resultSet);
                            } finally {
                                close(resultSet);
                            }
                        }
                    }
                }
        );
    }

    protected void read(Table table, ResultSet resultSet) throws SQLException {
        Map<Identifier, String> checks = Maps.newLinkedHashMap();
        while (resultSet.next()) {
            Identifier identifier = Identifier.valueOf(resultSet.getString("CONSTRAINT_NAME"));
            checks.put(identifier, resultSet.getString("CHECK_CLAUSE"));
        }
        table.setChecks(checks.values());
    }
}
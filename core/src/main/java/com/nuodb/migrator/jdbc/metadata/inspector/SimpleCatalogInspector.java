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

import com.nuodb.migrator.jdbc.metadata.Database;
import com.nuodb.migrator.jdbc.query.Query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.nuodb.migrator.jdbc.metadata.MetaDataType.CATALOG;
import static com.nuodb.migrator.jdbc.metadata.inspector.InspectionResultsUtils.addCatalog;

/**
 * @author Sergey Bushik
 */
public class SimpleCatalogInspector extends ManagedInspectorBase<Database, InspectionScope> {

    public SimpleCatalogInspector() {
        super(CATALOG, InspectionScope.class);
    }

    @Override
    protected ResultSet openResultSet(InspectionContext inspectionContext, InspectionScope inspectionScope)
            throws SQLException {
        return inspectionContext.getConnection().getMetaData().getCatalogs();
    }

    @Override
    protected void processResultSet(InspectionContext inspectionContext, ResultSet catalogs) throws SQLException {
        InspectionResults inspectionResults = inspectionContext.getInspectionResults();
        while (catalogs.next()) {
            addCatalog(inspectionResults, catalogs.getString("TABLE_CAT"));
        }
    }

    @Override
    protected InspectionScope createInspectionScope(Database database) {
        return null;
    }

    @Override
    public boolean supportsScope(InspectionContext inspectionContext, InspectionScope inspectionScope) {
        return true;
    }
}

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
package com.nuodb.migrator.cli;

/**
 * @author Sergey Bushik
 */
public interface CliResources {

    /**
     * Root options resources
     */
    final String ROOT_GROUP_NAME = "com.nuodb.migrator.root.group.name";

    final String HELP_OPTION_DESCRIPTION = "com.nuodb.migrator.help.option.description";
    final String HELP_ARGUMENT_NAME = "com.nuodb.migrator.help.argument.name";
    final String LIST_OPTION_DESCRIPTION = "com.nuodb.migrator.list.option.description";
    final String LIST_OPTION_OUTPUT = "com.nuodb.migrator.list.option.output";
    final String COMMAND_OPTION_DESCRIPTION = "com.nuodb.migrator.command.option.description";
    final String COMMAND_OPTION_HELP_VALUES = "com.nuodb.migrator.command.option.help.values";
    final String CONFIG_OPTION_DESCRIPTION = "com.nuodb.migrator.config.option.description";
    final String CONFIG_ARGUMENT_NAME = "com.nuodb.migrator.config.argument.name";

    /**
     * Dump plugin resources
     */
    final String DUMP_GROUP_NAME = "com.nuodb.migrator.dump.group.name";
    final String SOURCE_GROUP_NAME = "com.nuodb.migrator.source.group.name";
    final String SOURCE_DRIVER_OPTION_DESCRIPTION = "com.nuodb.migrator.source.driver.option.description";
    final String SOURCE_DRIVER_ARGUMENT_NAME = "com.nuodb.migrator.source.driver.argument.name";
    final String SOURCE_URL_OPTION_DESCRIPTION = "com.nuodb.migrator.source.url.option.description";
    final String SOURCE_URL_ARGUMENT_NAME = "com.nuodb.migrator.source.url.argument.name";
    final String SOURCE_USERNAME_OPTION_DESCRIPTION = "com.nuodb.migrator.source.username.option.description";
    final String SOURCE_USERNAME_ARGUMENT_NAME = "com.nuodb.migrator.source.username.argument.name";
    final String SOURCE_PASSWORD_OPTION_DESCRIPTION = "com.nuodb.migrator.source.password.option.description";
    final String SOURCE_PASSWORD_ARGUMENT_NAME = "com.nuodb.migrator.source.password.argument.name";
    final String SOURCE_PROPERTIES_OPTION_DESCRIPTION = "com.nuodb.migrator.source.properties.option.description";
    final String SOURCE_PROPERTIES_ARGUMENT_NAME = "com.nuodb.migrator.source.properties.argument.name";
    final String SOURCE_CATALOG_OPTION_DESCRIPTION = "com.nuodb.migrator.source.catalog.option.description";
    final String SOURCE_CATALOG_ARGUMENT_NAME = "com.nuodb.migrator.source.catalog.argument.name";
    final String SOURCE_SCHEMA_OPTION_DESCRIPTION = "com.nuodb.migrator.source.schema.option.description";
    final String SOURCE_SCHEMA_ARGUMENT_NAME = "com.nuodb.migrator.source.schema.argument.name";
    final String SOURCE_AUTO_COMMIT_OPTION_DESCRIPTION = "com.nuodb.migrator.source.auto.commit.option.description";
    final String SOURCE_AUTO_COMMIT_ARGUMENT_NAME = "com.nuodb.migrator.source.auto.commit.argument.name";

    final String OUTPUT_GROUP_NAME = "com.nuodb.migrator.output.group";
    final String OUTPUT_TYPE_OPTION_DESCRIPTION = "com.nuodb.migrator.output.type.option.description";
    final String OUTPUT_TYPE_ARGUMENT_NAME = "com.nuodb.migrator.output.type.argument.name";
    final String OUTPUT_PATH_OPTION_DESCRIPTION = "com.nuodb.migrator.output.path.option.description";
    final String OUTPUT_PATH_ARGUMENT_NAME = "com.nuodb.migrator.output.path.argument.name";
    final String OUTPUT_OPTION_DESCRIPTION = "com.nuodb.migrator.output.option.description";
    final String OUTPUT_OPTION_ARGUMENT_NAME = "com.nuodb.migrator.output.argument.description";

    final String TIME_ZONE_OPTION_DESCRIPTION = "com.nuodb.migrator.time.zone.option.description";
    final String TIME_ZONE_ARGUMENT_NAME = "com.nuodb.migrator.time.zone.argument.name";

    final String TABLE_GROUP_NAME = "com.nuodb.migrator.table.group.name";
    final String TABLE_OPTION_DESCRIPTION = "com.nuodb.migrator.table.option.description";
    final String TABLE_TYPE_OPTION_DESCRIPTION = "com.nuodb.migrator.table.type.option.description";
    final String TABLE_TYPE_ARGUMENT_NAME = "com.nuodb.migrator.table.type.argument.name";
    final String TABLE_ARGUMENT_NAME = "com.nuodb.migrator.table.argument.name";
    final String TABLE_FILTER_OPTION_DESCRIPTION = "com.nuodb.migrator.table.filter.option.description";
    final String TABLE_FILTER_ARGUMENT_NAME = "com.nuodb.migrator.table.filter.argument.name";

    final String QUERY_GROUP_NAME = "com.nuodb.migrator.query.group.name";
    final String QUERY_OPTION_DESCRIPTION = "com.nuodb.migrator.query.option.description";
    final String QUERY_ARGUMENT_NAME = "com.nuodb.migrator.query.argument.name";

    final String THREADS_OPTION_DESCRIPTION = "com.nuodb.migrator.threads.option.description";
    final String THREADS_ARGUMENT_NAME = "com.nuodb.migrator.threads.argument.name";
    final String QUERY_LIMIT_OPTION_DESCRIPTION = "com.nuodb.migrator.query.limit.option.description";
    final String QUERY_LIMIT_ARGUMENT_NAME = "com.nuodb.migrator.query.limit.argument.name";

    final String LOAD_GROUP_NAME = "com.nuodb.migrator.load.group.name";
    final String TARGET_GROUP_NAME = "com.nuodb.migrator.target.group.name";
    final String TARGET_URL_OPTION_DESCRIPTION = "com.nuodb.migrator.target.url.option.description";
    final String TARGET_URL_ARGUMENT_NAME = "com.nuodb.migrator.target.url.argument.name";
    final String TARGET_USERNAME_OPTION_DESCRIPTION = "com.nuodb.migrator.target.username.option.description";
    final String TARGET_USERNAME_ARGUMENT_NAME = "com.nuodb.migrator.target.username.argument.name";
    final String TARGET_PASSWORD_OPTION_DESCRIPTION = "com.nuodb.migrator.target.password.option.description";
    final String TARGET_PASSWORD_ARGUMENT_NAME = "com.nuodb.migrator.target.password.argument.name";
    final String TARGET_PROPERTIES_OPTION_DESCRIPTION = "com.nuodb.migrator.target.properties.option.description";
    final String TARGET_PROPERTIES_ARGUMENT_NAME = "com.nuodb.migrator.target.properties.argument.name";
    final String TARGET_SCHEMA_OPTION_DESCRIPTION = "com.nuodb.migrator.target.schema.option.description";
    final String TARGET_SCHEMA_ARGUMENT_NAME = "com.nuodb.migrator.target.schema.argument.name";
    final String TARGET_AUTO_COMMIT_OPTION_DESCRIPTION = "com.nuodb.migrator.target.auto.commit.option.description";
    final String TARGET_AUTO_COMMIT_ARGUMENT_NAME = "com.nuodb.migrator.target.auto.commit.argument.name";

    final String REPLACE_OPTION_DESCRIPTION = "com.nuodb.migrator.replace.option.description";

    final String INPUT_GROUP_NAME = "com.nuodb.migrator.input.group.name";
    final String INPUT_PATH_OPTION_DESCRIPTION = "com.nuodb.migrator.input.path.option.description";
    final String INPUT_PATH_ARGUMENT_NAME = "com.nuodb.migrator.input.path.argument.name";
    final String INPUT_OPTION_DESCRIPTION = "com.nuodb.migrator.input.option.description";
    final String INPUT_OPTION_ARGUMENT_NAME = "com.nuodb.migrator.input.argument.description";

    final String SCHEMA_GROUP_NAME = "com.nuodb.migrator.schema.group.name";
    final String SCHEMA_OUTPUT_GROUP_NAME = "com.nuodb.migrator.schema.output.group.name";
    final String SCHEMA_META_DATA_OPTION_DESCRIPTION = "com.nuodb.migrator.schema.meta.data.option.description";
    final String SCHEMA_META_DATA_ARGUMENT_NAME = "com.nuodb.migrator.schema.meta.data.argument.name";

    final String SCHEMA_SCRIPT_TYPE_OPTION_DESCRIPTION = "com.nuodb.migrator.schema.script.type.option.description";
    final String SCHEMA_SCRIPT_TYPE_ARGUMENT_NAME = "com.nuodb.migrator.schema.script.type.argument.name";

    final String SCHEMA_GROUP_SCRIPTS_BY_OPTION_DESCRIPTION = "com.nuodb.migrator.schema.group.scripts.by.option.description";
    final String SCHEMA_GROUP_SCRIPTS_BY_ARGUMENT_NAME = "com.nuodb.migrator.schema.group.scripts.by.argument.name";
    final String SCHEMA_IDENTIFIER_QUOTING_OPTION_DESCRIPTION = "com.nuodb.migrator.schema.identifier.quoting.option.description";
    final String SCHEMA_IDENTIFIER_QUOTING_ARGUMENT_NAME = "com.nuodb.migrator.schema.identifier.quoting.argument.name";
    final String SCHEMA_IDENTIFIER_NORMALIZER_OPTION_DESCRIPTION = "com.nuodb.migrator.schema.identifier.normalize.option.description";
    final String SCHEMA_IDENTIFIER_NORMALIZER_ARGUMENT_NAME = "com.nuodb.migrator.schema.identifier.normalizer.argument.name";

    final String JDBC_TYPE_GROUP_NAME = "com.nuodb.migrator.jdbc.type.group.name";
    final String JDBC_TYPE_CODE_OPTION_DESCRIPTION = "com.nuodb.migrator.jdbc.type.code.option.description";
    final String JDBC_TYPE_CODE_ARGUMENT_NAME = "com.nuodb.migrator.jdbc.type.code.argument.name";
    final String JDBC_TYPE_NAME_OPTION_DESCRIPTION = "com.nuodb.migrator.jdbc.type.name.option.description";
    final String JDBC_TYPE_NAME_ARGUMENT_NAME = "com.nuodb.migrator.jdbc.type.name.argument.name";
    final String JDBC_TYPE_SIZE_OPTION_DESCRIPTION = "com.nuodb.migrator.jdbc.type.size.option.description";
    final String JDBC_TYPE_SIZE_ARGUMENT_NAME = "com.nuodb.migrator.jdbc.type.size.argument.name";
    final String JDBC_TYPE_PRECISION_OPTION_DESCRIPTION = "com.nuodb.migrator.jdbc.type.precision.option.description";
    final String JDBC_TYPE_PRECISION_ARGUMENT_NAME = "com.nuodb.migrator.jdbc.type.precision.argument.name";
    final String JDBC_TYPE_SCALE_OPTION_DESCRIPTION = "com.nuodb.migrator.jdbc.type.scale.option.description";
    final String JDBC_TYPE_SCALE_ARGUMENT_NAME = "com.nuodb.migrator.jdbc.type.scale.argument.name";
    final String USE_NUODB_TYPES_SWITCH_DESCRIPTION = "com.nuodb.migrator.nuodb.types.switch.description";

    final String INSERT_TYPE_GROUP_NAME = "com.nuodb.migrator.insert.type.group.name";
    final String TABLE_REPLACE_OPTION_DESCRIPTION = "com.nuodb.migrator.table.replace.option.description";
    final String TABLE_INSERT_OPTION_DESCRIPTION = "com.nuodb.migrator.table.insert.option.description";
}

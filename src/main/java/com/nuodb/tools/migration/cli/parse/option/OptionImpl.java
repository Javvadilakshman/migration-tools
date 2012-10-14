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
package com.nuodb.tools.migration.cli.parse.option;

import com.nuodb.tools.migration.cli.parse.*;

import java.util.*;

import static com.nuodb.tools.migration.cli.parse.HelpHint.*;

/**
 * @author Sergey Bushik
 */
public class OptionImpl extends BaseContainer {

    private Set<String> prefixes;
    private Set<String> aliases;

    public OptionImpl() {
    }

    public OptionImpl(int id, String name, String description, boolean required) {
        super(id, name, description, required);
    }

    public OptionImpl(int id, String name, String description, boolean required,
                      Set<String> prefixes, Set<String> aliases) {
        super(id, name, description, required);
        this.prefixes = prefixes;
        this.aliases = aliases;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public void setAliases(Set<String> aliases) {
        this.aliases = aliases;
    }

    @Override
    public Set<String> getPrefixes() {
        Set<String> prefixes = new HashSet<String>();
        Group group = getGroup();
        if (group != null) {
            prefixes.addAll(group.getPrefixes());
        }
        if (this.prefixes != null) {
            prefixes.addAll(this.prefixes);
        }
        return prefixes;
    }

    public void setPrefixes(Set<String> prefixes) {
        this.prefixes = prefixes;
    }

    @Override
    public Set<Trigger> getTriggers() {
        Set<Trigger> triggers = new HashSet<Trigger>();
        triggers.addAll(super.getTriggers());

        Set<String> prefixes = getPrefixes();
        addTriggers(triggers, prefixes, getName());
        Set<String> aliases = getAliases();
        if (aliases != null) {
            for (String alias : aliases) {
                addTriggers(triggers, prefixes, alias);
            }
        }
        return triggers;
    }

    @Override
    protected void doProcess(CommandLine commandLine, ListIterator<String> arguments) {
        String argument = arguments.next();
        Trigger trigger = fire(getTriggers(), argument);
        if (trigger != null) {
            doProcess(commandLine, trigger, argument);
        } else {
            throw new OptionException(this, String.format("Unexpected token %1$s", argument));
        }
    }

    protected void doProcess(CommandLine commandLine, Trigger trigger, String argument) {
        commandLine.addOption(this);
    }

    @Override
    public void help(StringBuilder help, Set<HelpHint> hints, Comparator<Option> comparator) {
        boolean optional = !isRequired() && hints.contains(OPTIONAL);
        boolean displayAliases = hints.contains(ALIASES);
        if (optional) {
            help.append('[');
        }
        Set<String> prefixes = getPrefixes();
        Set<Trigger> triggers = new HashSet<Trigger>();
        addTriggers(triggers, prefixes, getName());
        joinTriggers(help, triggers);
        Set<String> aliases = getAliases();
        if (displayAliases && (aliases != null && !aliases.isEmpty())) {
            help.append(" (");
            List<String> list = new ArrayList<String>(aliases);
            Collections.sort(list);
            for (Iterator<String> i = list.iterator(); i.hasNext(); ) {
                String alias = i.next();
                triggers = new HashSet<Trigger>();
                addTriggers(triggers, prefixes, alias);
                joinTriggers(help, triggers);
                if (i.hasNext()) {
                    help.append(',');
                }
            }
            help.append(')');
        }
        Argument argument = getArgument();
        boolean displayArgument = argument != null && hints.contains(CONTAINER_ARGUMENT);
        Group children = getGroup();
        boolean displayChildren = children != null && hints.contains(CONTAINER_GROUP);
        if (displayArgument) {
            help.append(getArgumentSeparator());
            argument.help(help, hints, comparator);
        }
        if (displayChildren) {
            help.append(' ');
            children.help(help, hints, comparator);
        }

        if (optional) {
            help.append("]");
        }
    }

    protected void addTriggers(Set<Trigger> triggers, Set<String> prefixes, String trigger) {
        for (String prefix : prefixes) {
            triggers.add(new TriggerImpl(prefix + trigger));
        }
    }

    protected void joinTriggers(StringBuilder help, Set<Trigger> triggers) {
        for (Iterator<Trigger> iterator = triggers.iterator(); iterator.hasNext(); ) {
            help.append(iterator.next());
            if (iterator.hasNext()) {
                help.append(",");
            }
        }
    }
}

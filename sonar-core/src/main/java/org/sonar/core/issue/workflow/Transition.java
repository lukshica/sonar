/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2013 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.core.issue.workflow;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.issue.Issue;
import org.sonar.api.issue.condition.Condition;

import java.util.Arrays;
import java.util.List;

public class Transition {
  private final String key;
  private final String from, to;
  private final Condition[] conditions;
  private final Function[] functions;
  private final boolean automatic;

  private Transition(TransitionBuilder builder) {
    key = builder.key;
    from = builder.from;
    to = builder.to;
    conditions = builder.conditions.toArray(new Condition[builder.conditions.size()]);
    functions = builder.functions.toArray(new Function[builder.functions.size()]);
    automatic = builder.automatic;
  }

  public String key() {
    return key;
  }

  String from() {
    return from;
  }

  String to() {
    return to;
  }

  Condition[] conditions() {
    return conditions;
  }

  Function[] functions() {
    return functions;
  }

  boolean automatic() {
    return automatic;
  }

  public boolean supports(Issue issue) {
    for (Condition condition : conditions) {
      if (!condition.matches(issue)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Transition that = (Transition) o;
    if (!from.equals(that.from)) {
      return false;
    }
    if (!key.equals(that.key)) {
      return false;
    }
    if (!to.equals(that.to)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = key.hashCode();
    result = 31 * result + from.hashCode();
    result = 31 * result + to.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String.format("%s->%s->%s", from, key, to);
  }

  public static Transition create(String key, String from, String to) {
    return builder(key).from(from).to(to).build();
  }

  public static TransitionBuilder builder(String key) {
    return new TransitionBuilder(key);
  }

  public static class TransitionBuilder {
    private final String key;
    private String from, to;
    private List<Condition> conditions = Lists.newArrayList();
    private List<Function> functions = Lists.newArrayList();
    private boolean automatic = false;

    private TransitionBuilder(String key) {
      this.key = key;
    }

    public TransitionBuilder from(String from) {
      this.from = from;
      return this;
    }

    public TransitionBuilder to(String to) {
      this.to = to;
      return this;
    }

    public TransitionBuilder conditions(Condition... c) {
      this.conditions.addAll(Arrays.asList(c));
      return this;
    }

    public TransitionBuilder functions(Function... f) {
      this.functions.addAll(Arrays.asList(f));
      return this;
    }

    public TransitionBuilder automatic() {
      this.automatic = true;
      return this;
    }

    public Transition build() {
      Preconditions.checkArgument(!Strings.isNullOrEmpty(key), "Transition key must be set");
      Preconditions.checkArgument(StringUtils.isAllLowerCase(key), "Transition key must be lower-case");
      Preconditions.checkArgument(!Strings.isNullOrEmpty(from), "Originating status must be set");
      Preconditions.checkArgument(!Strings.isNullOrEmpty(to), "Destination status must be set");
      return new Transition(this);
    }
  }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.commons.core.edm;

import org.apache.olingo.commons.api.edm.EdmException;
import org.apache.olingo.commons.api.edm.FullQualifiedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FunctionMapKey {

  private final FullQualifiedName functionName;

  private final FullQualifiedName bindingParameterTypeName;

  private final Boolean isBindingParameterCollection;

  private final List<String> parameterNames;

  public FunctionMapKey(final FullQualifiedName functionName, final FullQualifiedName bindingParameterTypeName,
      final Boolean isBindingParameterCollection, final List<String> parameterNames) {

    this.functionName = functionName;
    if (bindingParameterTypeName != null && isBindingParameterCollection == null) {
      throw new EdmException(
          "Indicator that the bindingparameter is a collection must not be null if its an bound function.");
    }
    this.bindingParameterTypeName = bindingParameterTypeName;
    this.isBindingParameterCollection = isBindingParameterCollection;
    this.parameterNames = new ArrayList<String>();
    if (parameterNames != null) {
      this.parameterNames.addAll(parameterNames);
      Collections.sort(this.parameterNames);
    }
  }

  @Override
  public int hashCode() {
    String hash = functionName.toString();

    if (bindingParameterTypeName != null) {
      hash = hash + bindingParameterTypeName.toString();
    } else {
      hash = hash + "typeNull";
    }

    if (isBindingParameterCollection != null) {
      hash = hash + isBindingParameterCollection.toString();
    } else {
      hash = hash + "collectionNull";
    }

    if (!parameterNames.isEmpty()) {
      for (String name : parameterNames) {
        hash = hash + name;
      }
    } else {
      hash = hash + "parameterNamesEmpty";
    }

    return hash.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj == null) || !(obj instanceof FunctionMapKey)) {
      return false;
    }
    final FunctionMapKey other = (FunctionMapKey) obj;

    if (functionName.equals(other.functionName)
        && (bindingParameterTypeName == null && other.bindingParameterTypeName == null)
        || (bindingParameterTypeName != null && bindingParameterTypeName.equals(other.bindingParameterTypeName))
        && (isBindingParameterCollection == null
        && other.isBindingParameterCollection == null)
        || (isBindingParameterCollection != null
        && isBindingParameterCollection.equals(other.isBindingParameterCollection))) {

      if (parameterNames == null && other.parameterNames == null) {
        return true;
      } else if (parameterNames.size() == other.parameterNames.size()) {
        for (String name : parameterNames) {
          if (!other.parameterNames.contains(name)) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }
}

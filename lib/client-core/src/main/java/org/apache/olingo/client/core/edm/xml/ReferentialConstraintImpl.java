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
package org.apache.olingo.client.core.edm.xml;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.olingo.commons.api.edm.provider.ReferentialConstraint;

import java.io.IOException;

@JsonDeserialize(using = ReferentialConstraintImpl.ReferentialConstraintDeserializer.class)
public class ReferentialConstraintImpl extends ReferentialConstraint {

  private static final long serialVersionUID = -5822115908069878139L;

  static class ReferentialConstraintDeserializer extends AbstractEdmDeserializer<ReferentialConstraint> {
    @Override
    protected ReferentialConstraint doDeserialize(final JsonParser jp, final DeserializationContext ctxt)
            throws IOException {

      final ReferentialConstraint refConst = new ReferentialConstraintImpl();

      for (; jp.getCurrentToken() != JsonToken.END_OBJECT; jp.nextToken()) {
        final JsonToken token = jp.getCurrentToken();
        if (token == JsonToken.FIELD_NAME) {
          if ("Property".equals(jp.getCurrentName())) {
            refConst.setProperty(jp.nextTextValue());
          } else if ("ReferencedProperty".equals(jp.getCurrentName())) {
            refConst.setReferencedProperty(jp.nextTextValue());
          }
        }
      }
      return refConst;
    }
  }
}

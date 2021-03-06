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
package org.apache.olingo.commons.core.domain;

import org.apache.olingo.commons.api.domain.ODataDeletedEntity;
import org.apache.olingo.commons.api.domain.ODataDelta;
import org.apache.olingo.commons.api.domain.ODataDeltaLink;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ODataDeltaImpl extends ODataEntitySetImpl implements ODataDelta {

  private final List<ODataDeletedEntity> deletedEntities = new ArrayList<ODataDeletedEntity>();

  private final List<ODataDeltaLink> addedLinks = new ArrayList<ODataDeltaLink>();

  private final List<ODataDeltaLink> deletedLinks = new ArrayList<ODataDeltaLink>();

  public ODataDeltaImpl() {
    super();
  }

  public ODataDeltaImpl(final URI next) {
    super(next);
  }

  @Override
  public List<ODataDeletedEntity> getDeletedEntities() {
    return deletedEntities;
  }

  @Override
  public List<ODataDeltaLink> getAddedLinks() {
    return addedLinks;
  }

  @Override
  public List<ODataDeltaLink> getDeletedLinks() {
    return deletedLinks;
  }

}

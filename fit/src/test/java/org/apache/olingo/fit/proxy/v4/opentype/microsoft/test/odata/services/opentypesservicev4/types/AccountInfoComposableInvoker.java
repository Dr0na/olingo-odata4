/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.fit.proxy.v4.opentype.microsoft.test.odata.services.opentypesservicev4.types;

//CHECKSTYLE:OFF (Maven checkstyle)
import org.apache.olingo.ext.proxy.api.AbstractOpenType;
//CHECKSTYLE:ON (Maven checkstyle)


public interface AccountInfoComposableInvoker 
  extends org.apache.olingo.ext.proxy.api.StructuredComposableInvoker<AccountInfo, AccountInfo.Operations>
  ,AbstractOpenType {

  @Override
  AccountInfoComposableInvoker select(String... select);

  @Override
  AccountInfoComposableInvoker expand(String... expand);


    @org.apache.olingo.ext.proxy.api.annotations.Property(name = "FirstName", 
                type = "Edm.String",
                nullable = false,
                defaultValue = "",
                maxLenght = Integer.MAX_VALUE,
                fixedLenght = false,
                precision = 0,
                scale = 0,
                unicode = true,
                collation = "",
                srid = "")
    java.lang.String getFirstName();

    void setFirstName(java.lang.String _firstName);

    

    @org.apache.olingo.ext.proxy.api.annotations.Property(name = "LastName", 
                type = "Edm.String",
                nullable = false,
                defaultValue = "",
                maxLenght = Integer.MAX_VALUE,
                fixedLenght = false,
                precision = 0,
                scale = 0,
                unicode = true,
                collation = "",
                srid = "")
    java.lang.String getLastName();

    void setLastName(java.lang.String _lastName);

    




}

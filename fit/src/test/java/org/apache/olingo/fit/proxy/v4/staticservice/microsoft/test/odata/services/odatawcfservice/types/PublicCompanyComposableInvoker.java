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
package org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types;
//CHECKSTYLE:OFF (Maven checkstyle)
import org.apache.olingo.ext.proxy.api.annotations.Key;
import org.apache.olingo.ext.proxy.api.AbstractOpenType;
import org.apache.olingo.ext.proxy.api.AbstractEntitySet;
//CHECKSTYLE:ON (Maven checkstyle)

public interface PublicCompanyComposableInvoker 
  extends org.apache.olingo.ext.proxy.api.StructuredComposableInvoker<PublicCompany, PublicCompany.Operations>
  ,AbstractOpenType {

  @Override
  PublicCompanyComposableInvoker select(String... select);

  @Override
  PublicCompanyComposableInvoker expand(String... expand);

    

    @Key
    
    @org.apache.olingo.ext.proxy.api.annotations.Property(name = "CompanyID", 
                type = "Edm.Int32", 
                nullable = false,
                defaultValue = "",
                maxLenght = Integer.MAX_VALUE,
                fixedLenght = false,
                precision = 0,
                scale = 0,
                unicode = true,
                collation = "",
                srid = "")
    java.lang.Integer getCompanyID();

    void setCompanyID(java.lang.Integer _companyID);
    
    
    @org.apache.olingo.ext.proxy.api.annotations.Property(name = "CompanyCategory", 
                type = "Microsoft.Test.OData.Services.ODataWCFService.CompanyCategory", 
                nullable = true,
                defaultValue = "",
                maxLenght = Integer.MAX_VALUE,
                fixedLenght = false,
                precision = 0,
                scale = 0,
                unicode = true,
                collation = "",
                srid = "")
    org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.CompanyCategory getCompanyCategory();

    void setCompanyCategory(org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.CompanyCategory _companyCategory);
    
    
    @org.apache.olingo.ext.proxy.api.annotations.Property(name = "Revenue", 
                type = "Edm.Int64", 
                nullable = false,
                defaultValue = "",
                maxLenght = Integer.MAX_VALUE,
                fixedLenght = false,
                precision = 0,
                scale = 0,
                unicode = true,
                collation = "",
                srid = "")
    java.lang.Long getRevenue();

    void setRevenue(java.lang.Long _revenue);
    
    
    @org.apache.olingo.ext.proxy.api.annotations.Property(name = "Name", 
                type = "Edm.String", 
                nullable = true,
                defaultValue = "",
                maxLenght = Integer.MAX_VALUE,
                fixedLenght = false,
                precision = 0,
                scale = 0,
                unicode = true,
                collation = "",
                srid = "")
    java.lang.String getName();

    void setName(java.lang.String _name);
    
    
    @org.apache.olingo.ext.proxy.api.annotations.Property(name = "Address", 
                type = "Microsoft.Test.OData.Services.ODataWCFService.Address", 
                nullable = true,
                defaultValue = "",
                maxLenght = Integer.MAX_VALUE,
                fixedLenght = false,
                precision = 0,
                scale = 0,
                unicode = true,
                collation = "",
                srid = "")
    org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.Address getAddress();

    void setAddress(org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.Address _address);
    
    
    @org.apache.olingo.ext.proxy.api.annotations.Property(name = "StockExchange", 
                type = "Edm.String", 
                nullable = true,
                defaultValue = "",
                maxLenght = Integer.MAX_VALUE,
                fixedLenght = false,
                precision = 0,
                scale = 0,
                unicode = true,
                collation = "",
                srid = "")
    java.lang.String getStockExchange();

    void setStockExchange(java.lang.String _stockExchange);
    

    @org.apache.olingo.ext.proxy.api.annotations.NavigationProperty(name = "Employees", 
                type = "Microsoft.Test.OData.Services.ODataWCFService.Employee", 
                targetSchema = "Microsoft.Test.OData.Services.ODataWCFService", 
                targetContainer = "InMemoryEntities", 
                targetEntitySet = "Employees",
                containsTarget = false)
    org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.EmployeeCollection getEmployees();

    void setEmployees(org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.EmployeeCollection _employees);
    
    @org.apache.olingo.ext.proxy.api.annotations.NavigationProperty(name = "VipCustomer", 
                type = "Microsoft.Test.OData.Services.ODataWCFService.Customer", 
                targetSchema = "Microsoft.Test.OData.Services.ODataWCFService", 
                targetContainer = "InMemoryEntities", 
                targetEntitySet = "VipCustomer",
                containsTarget = false)
    org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.Customer getVipCustomer();

    void setVipCustomer(org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.Customer _vipCustomer);
    
    @org.apache.olingo.ext.proxy.api.annotations.NavigationProperty(name = "Departments", 
                type = "Microsoft.Test.OData.Services.ODataWCFService.Department", 
                targetSchema = "Microsoft.Test.OData.Services.ODataWCFService", 
                targetContainer = "InMemoryEntities", 
                targetEntitySet = "Departments",
                containsTarget = false)
    org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.DepartmentCollection getDepartments();

    void setDepartments(org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.DepartmentCollection _departments);
    
    @org.apache.olingo.ext.proxy.api.annotations.NavigationProperty(name = "CoreDepartment", 
                type = "Microsoft.Test.OData.Services.ODataWCFService.Department", 
                targetSchema = "Microsoft.Test.OData.Services.ODataWCFService", 
                targetContainer = "InMemoryEntities", 
                targetEntitySet = "Departments",
                containsTarget = false)
    org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.Department getCoreDepartment();

    void setCoreDepartment(org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.Department _coreDepartment);
    
        
    @org.apache.olingo.ext.proxy.api.annotations.NavigationProperty(name = "Club", 
                type = "Microsoft.Test.OData.Services.ODataWCFService.Club", 
                targetSchema = "Microsoft.Test.OData.Services.ODataWCFService", 
                targetContainer = "", 
                targetEntitySet = "",
                containsTarget = true)
    org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.Club getClub();

    void setClub(org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.Club _club);
    
    @org.apache.olingo.ext.proxy.api.annotations.NavigationProperty(name = "LabourUnion", 
                type = "Microsoft.Test.OData.Services.ODataWCFService.LabourUnion", 
                targetSchema = "Microsoft.Test.OData.Services.ODataWCFService", 
                targetContainer = "InMemoryEntities", 
                targetEntitySet = "LabourUnion",
                containsTarget = false)
    org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.LabourUnion getLabourUnion();

    void setLabourUnion(org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.LabourUnion _labourUnion);
    

      @org.apache.olingo.ext.proxy.api.annotations.NavigationProperty(name = "Assets", 
                type = "Microsoft.Test.OData.Services.ODataWCFService.LabourUnion", 
                targetSchema = "Microsoft.Test.OData.Services.ODataWCFService", 
                targetContainer = "InMemoryEntities", 
                targetEntitySet = "LabourUnion",
                containsTarget = true)
    org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.PublicCompany.Assets getAssets();
    void setAssets(org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.PublicCompany.Assets _assets);

            
    
    @org.apache.olingo.ext.proxy.api.annotations.EntitySet(name = "Assets", contained = true)
    interface Assets 
      extends org.apache.olingo.ext.proxy.api.EntitySet<org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.Asset, org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.AssetCollection>, 
      org.apache.olingo.ext.proxy.api.StructuredCollectionQuery<Assets>,
      AbstractEntitySet<org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.Asset, java.lang.Integer, org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.AssetCollection> {
    //No additional methods needed for now.
    }

  }

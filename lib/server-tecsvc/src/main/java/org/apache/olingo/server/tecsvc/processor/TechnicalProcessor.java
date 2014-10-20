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
package org.apache.olingo.server.tecsvc.processor;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntitySet;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmReturnType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.EntitySetProcessor;
import org.apache.olingo.server.api.processor.ProcedureProcessor;
import org.apache.olingo.server.api.processor.PropertyProcessor;
import org.apache.olingo.server.api.serializer.BoundProcedureOption;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.ODataSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceAction;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.tecsvc.data.DataProvider;

/**
 * Technical Processor which provides currently implemented processor functionality.
 */
public class TechnicalProcessor implements EntitySetProcessor, EntityProcessor, PropertyProcessor, ProcedureProcessor {

  private OData odata;
  private DataProvider dataProvider;
  private ServiceMetadata edm;

  public TechnicalProcessor(final DataProvider dataProvider) {
    this.dataProvider = dataProvider;    
  }

  @Override
  public void init(final OData odata, final ServiceMetadata edm) {
    this.odata = odata;
    this.edm = edm;
  }

  @Override
  public void readEntitySet(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
      final ContentType requestedContentType) throws ODataApplicationException, SerializerException {
    validateOptions(uriInfo.asUriInfoResource());

    final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());
    final EntitySet entitySet = readEntitySetInternal(edmEntitySet,
        uriInfo.getCountOption() != null && uriInfo.getCountOption().getValue());
    if (entitySet == null) {
      throw new ODataApplicationException("Nothing found.", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
    } else {
      final ODataFormat format = ODataFormat.fromContentType(requestedContentType);
      ODataSerializer serializer = odata.createSerializer(format);
      final ExpandOption expand = uriInfo.getExpandOption();
      final SelectOption select = uriInfo.getSelectOption();
      response.setContent(serializer.entitySet(edmEntitySet, entitySet,
          ODataSerializerOptions.with()
              .contextURL(format == ODataFormat.JSON_NO_METADATA ? null :
                  getContextUrl(serializer, edmEntitySet, false, expand, select, null))
              .count(uriInfo.getCountOption())
              .expand(expand).select(select)
              .build()));
      response.setStatusCode(HttpStatusCode.OK.getStatusCode());
      response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
    }
  }

  @Override
  public void readEntity(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
      final ContentType requestedContentType) throws ODataApplicationException, SerializerException {
    validateOptions(uriInfo.asUriInfoResource());

    final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());
    final Entity entity = readEntityInternal(uriInfo.asUriInfoResource(), edmEntitySet);
    if (entity == null) {
      throw new ODataApplicationException("Nothing found.", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
    } else {
      final ODataFormat format = ODataFormat.fromContentType(requestedContentType);
      ODataSerializer serializer = odata.createSerializer(format);
      final ExpandOption expand = uriInfo.getExpandOption();
      final SelectOption select = uriInfo.getSelectOption();
      response.setContent(serializer.entity(edmEntitySet, entity,
          ODataSerializerOptions.with()
              .contextURL(format == ODataFormat.JSON_NO_METADATA ? null :
                  getContextUrl(serializer, edmEntitySet, true, expand, select, null))
              .count(uriInfo.getCountOption())
              .expand(expand).select(select)
              .build()));
      response.setStatusCode(HttpStatusCode.OK.getStatusCode());
      response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
    }
  }

  @Override
  public void countEntitySet(final ODataRequest request, ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, SerializerException {
    final List<UriResource> resourceParts = uriInfo.asUriInfoResource().getUriResourceParts();
    final int pos = resourceParts.size() - 2;
    final EntitySet entitySet =
        readEntitySetInternal(((UriResourceEntitySet) resourceParts.get(pos)).getEntitySet(), true);
    if (entitySet == null) {
      throw new ODataApplicationException("Nothing found.", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
    } else {
      response.setContent(new ByteArrayInputStream(entitySet.getCount().toString().getBytes()));
      response.setStatusCode(HttpStatusCode.OK.getStatusCode());
      response.setHeader(HttpHeader.CONTENT_TYPE, HttpContentType.TEXT_PLAIN);
    }
  }

  private EntitySet readEntitySetInternal(final EdmEntitySet edmEntitySet,
      final boolean withCount) throws DataProvider.DataProviderException {
    EntitySet entitySet = dataProvider.readAll(edmEntitySet);
    // TODO: set count (correctly) and next link
    if (withCount && entitySet.getCount() == null) {
      entitySet.setCount(entitySet.getEntities().size());
    }
    return entitySet;
  }

  private Entity readEntityInternal(final UriInfoResource uriInfo, final EdmEntitySet entitySet)
      throws DataProvider.DataProviderException {
    final UriResourceEntitySet resourceEntitySet = (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
    return dataProvider.read(entitySet, resourceEntitySet.getKeyPredicates());
  }

  private void validateOptions(final UriInfoResource uriInfo) throws ODataApplicationException {
    if (uriInfo.getCountOption() != null
        || !uriInfo.getCustomQueryOptions().isEmpty()
        || uriInfo.getFilterOption() != null
        || uriInfo.getIdOption() != null
        || uriInfo.getOrderByOption() != null
        || uriInfo.getSearchOption() != null
        || uriInfo.getSkipOption() != null
        || uriInfo.getSkipTokenOption() != null
        || uriInfo.getTopOption() != null) {
      throw new ODataApplicationException("Not all of the specified options are supported.",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }
  }

  private EdmEntitySet getEdmEntitySet(final UriInfoResource uriInfo) throws ODataApplicationException {
    final List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
    // first must be entity set
    if (!(resourcePaths.get(0) instanceof UriResourceEntitySet)) {
      throw new ODataApplicationException("Invalid resource type.",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }
    List<UriResource> subResPaths = resourcePaths.subList(1, resourcePaths.size());
    for (UriResource subResPath : subResPaths) {
      UriResourceKind kind = subResPath.getKind();
      if(kind != UriResourceKind.primitiveProperty
              && kind != UriResourceKind.complexProperty
              && kind != UriResourceKind.count
              && kind != UriResourceKind.value) {
        throw new ODataApplicationException("Invalid resource type.",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
      }
    }

    final UriResourceEntitySet uriResource = (UriResourceEntitySet) resourcePaths.get(0);
    if (uriResource.getTypeFilterOnCollection() != null || uriResource.getTypeFilterOnEntry() != null) {
      throw new ODataApplicationException("Type filters are not supported.",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }
    return uriResource.getEntitySet();
  }

  private ContextURL getContextUrl(final ODataSerializer serializer,
      final EdmEntitySet entitySet, final boolean isSingleEntity,
      final ExpandOption expand, final SelectOption select, final String propertyPath)
    throws SerializerException {
    return ContextURL.with().entitySet(entitySet)
        .selectList(serializer.buildContextURLSelectList(entitySet, expand, select))
        .suffix(isSingleEntity && propertyPath == null ? Suffix.ENTITY : null)
        .navOrPropertyPath(propertyPath)
        .build();
  }

  private Map<String, String> mapKeys(List<UriParameter> parameters)
          throws ODataApplicationException {
    Map<String, String> keys = new LinkedHashMap<String, String>();
    for (UriParameter param: parameters) {
      keys.put(param.getName(), param.getText());
    }
    return keys;
  }

  @Override
  public void readProperty(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
      final ContentType contentType) throws ODataApplicationException, SerializerException {
    validateOptions(uriInfo.asUriInfoResource());

    final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());
    final UriResourceEntitySet resourceEntitySet = (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
    final Entity entity = readEntityInternal(uriInfo.asUriInfoResource(), edmEntitySet);

    if (entity == null) {
      throw new ODataApplicationException("Nothing found.", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
    } else {
      final UriResourceProperty uriProperty = (UriResourceProperty) uriInfo
          .getUriResourceParts().get(uriInfo.getUriResourceParts().size() - 1);
      final EdmProperty edmProperty = uriProperty.getProperty();
      final Property property = entity.getProperty(edmProperty.getName());
      if (property == null) {
        response.setStatusCode(HttpStatusCode.NOT_FOUND.getStatusCode());
      } else {
        if (property.getValue() == null) {
          response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
        } else {
          final ODataFormat format = ODataFormat.fromContentType(contentType);
          ODataSerializer serializer = odata.createSerializer(format);
          response.setContent(serializer.entityProperty(edmProperty, property,
                  ODataSerializerOptions.with().contextURL(format == ODataFormat.JSON_NO_METADATA ? null :
                          ContextURL.with().entitySet(edmEntitySet)
                                  .keySegment(mapKeys(resourceEntitySet.getKeyPredicates()))
                                  .navOrPropertyPath(edmProperty.getName())
                                  .build()).build()));
          response.setStatusCode(HttpStatusCode.OK.getStatusCode());
          response.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());
        }
      }
    }
  }

  @Override
  public void readPropertyValue(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
      final ContentType contentType) throws ODataApplicationException, SerializerException {
    validateOptions(uriInfo.asUriInfoResource());

    final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());
    final Entity entity = readEntityInternal(uriInfo.asUriInfoResource(), edmEntitySet);
    if (entity == null) {
      response.setStatusCode(HttpStatusCode.NOT_FOUND.getStatusCode());
    } else {
      final UriResourceProperty uriProperty =
          (UriResourceProperty) uriInfo.getUriResourceParts().get(uriInfo.getUriResourceParts().size() - 2);
      final EdmProperty edmProperty = uriProperty.getProperty();
      final Property property = entity.getProperty(edmProperty.getName());
      if (property == null || property.getValue() == null) {
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
      } else {
        final EdmPrimitiveType type = (EdmPrimitiveType) edmProperty.getType();
        try {
          final String value = type.valueToString(property.getValue(),
              edmProperty.isNullable(), edmProperty.getMaxLength(),
              edmProperty.getPrecision(), edmProperty.getScale(), edmProperty.isUnicode());
          response.setContent(new ByteArrayInputStream(value.getBytes("UTF-8")));
        } catch (final EdmPrimitiveTypeException e) {
          throw new ODataApplicationException("Error in value formatting.",
              HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT, e);
        } catch (final UnsupportedEncodingException e) {
          throw new ODataApplicationException("Encoding exception.",
              HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT, e);
        }
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.TEXT_PLAIN.toContentTypeString());
      }
    }
  }

  @Override
  public void executeFunction(ODataRequest request, ODataResponse response,
      UriInfo uriInfo, ContentType requestedContentType) throws ODataApplicationException, SerializerException {
    final List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
    Object result = null;

    UriResourceFunction functionURI = null;
    for (UriResource uriResource:resourcePaths) {
      if (uriResource instanceof UriResourceFunction) {
        functionURI = (UriResourceFunction)uriResource;
        break;
      }
    }
    
    if (functionURI == null) {
      throw new ODataApplicationException("Function not found",
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT);
    }
    
    ODataSerializerOptions.Builder options = ODataSerializerOptions.with();
    if (functionURI.getFunction().isBound()) {
      options.boundProcedure(BoundProcedureOption.with()
          .setFunction(functionURI.getFunction())
          .setTarget(request.getRawODataPath())
          .setTitle(functionURI.getFunction().getName()).build());
    }

    EdmFunction edmFunction = functionURI.getFunction();
    result = dataProvider.invokeFunction(functionURI);
    if (result == null) {
      response.setStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode());
    } else {
        final ODataFormat format = ODataFormat.fromContentType(requestedContentType);
        ODataSerializer serializer = odata.createSerializer(format);      
        EdmReturnType returnType = edmFunction.getReturnType();
        switch(returnType.getType().getKind()) {
        case PRIMITIVE:          
        case COMPLEX:
          ContextURL.Builder contextURL = ContextURL.with().propertyType(returnType.getType());
          if (returnType.isCollection()) {
            contextURL.asCollection();
          }
          response.setContent(serializer.procedureReturn(returnType, (Property)result,
              options.contextURL(format == ODataFormat.JSON_NO_METADATA ? null :
                contextURL.build()).build()));
          response.setStatusCode(HttpStatusCode.OK.getStatusCode());
          response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());          
          break;
        case ENTITY:
          EdmEntitySet edmEntitySet = null;
          if (edmFunction.isBound()) {
            //TODO: this needs to be fixed to return correct entitySet
            edmEntitySet = edmFunction.getReturnedEntitySet(null);
          } else {
            edmEntitySet = functionURI.getFunctionImport().getReturnedEntitySet();
          }
          if (edmEntitySet == null) {
            throw new ODataApplicationException("EntitySet type not defined on function",
                HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT);
          }          
          if (returnType.isCollection()) {
            response.setContent(serializer.entitySet(edmEntitySet, (EntitySet)result,                 
                options.contextURL(format == ODataFormat.JSON_NO_METADATA ? null: 
                    getContextUrl(serializer, edmEntitySet, false, null, null, null)).build()));           
          } else {
            response.setContent(serializer.entity(edmEntitySet, (Entity) result,
                options.contextURL(format == ODataFormat.JSON_NO_METADATA ? null: 
                      getContextUrl(serializer, edmEntitySet, true, null, null, null)).build()));           
          }
          response.setStatusCode(HttpStatusCode.OK.getStatusCode());
          response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
          break;
        default:
          throw new ODataApplicationException("Return type not supported",
              HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT);
        }    
    }       
  }

  @Override
  public void executeAction(ODataRequest request, ODataResponse response,
      UriInfo uriInfo, ContentType requestedContentType) throws ODataApplicationException, SerializerException {
    final List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
    Object result = null;

    UriResourceAction actionURI = null;
    for (UriResource uriResource:resourcePaths) {
      if (uriResource instanceof UriResourceAction) {
        actionURI = (UriResourceAction)uriResource;
        break;
      }
    }
    
    if (actionURI == null) {
      throw new ODataApplicationException("Action not found",
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT);
    }
    
    ODataSerializerOptions.Builder options = ODataSerializerOptions.with();
    if (actionURI.getAction().isBound()) {
      options.boundProcedure(BoundProcedureOption.with()
          .setAction(actionURI.getAction())
          .setTarget(request.getRawODataPath())
          .setTitle(actionURI.getAction().getName()).build());
    }

    EdmAction edmAction = actionURI.getAction();
    result = dataProvider.invokeAction(actionURI);
    if (result == null) {
      response.setStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode());
    } else {
        final ODataFormat format = ODataFormat.fromContentType(requestedContentType);
        ODataSerializer serializer = odata.createSerializer(format);      
        EdmReturnType returnType = edmAction.getReturnType();
        switch(returnType.getType().getKind()) {
        case PRIMITIVE:          
        case COMPLEX:
          ContextURL.Builder contextURL = ContextURL.with().propertyType(returnType.getType());
          if (returnType.isCollection()) {
            contextURL.asCollection();
          }
          response.setContent(serializer.procedureReturn(returnType, (Property)result,
              options.contextURL(format == ODataFormat.JSON_NO_METADATA ? null :
                contextURL.build()).build()));
          response.setStatusCode(HttpStatusCode.OK.getStatusCode());
          response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());          
          break;
        case ENTITY:
          EdmEntitySet edmEntitySet = null;
          if (edmAction.isBound()) {
            //TODO: this needs to be fixed to return correct entitySet
            edmEntitySet = edmAction.getReturnedEntitySet(null);
          } else {
            edmEntitySet = actionURI.getActionImport().getReturnedEntitySet();
          }
          if (edmEntitySet == null) {
            throw new ODataApplicationException("EntitySet type not defined on function",
                HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT);
          }          
          if (returnType.isCollection()) {
            response.setContent(serializer.entitySet(edmEntitySet, (EntitySet)result,                 
                options.contextURL(format == ODataFormat.JSON_NO_METADATA ? null: 
                    getContextUrl(serializer, edmEntitySet, false, null, null, null)).build()));           
          } else {
            response.setContent(serializer.entity(edmEntitySet, (Entity) result,
                options.contextURL(format == ODataFormat.JSON_NO_METADATA ? null: 
                      getContextUrl(serializer, edmEntitySet, true, null, null, null)).build()));           
          }
          response.setStatusCode(HttpStatusCode.OK.getStatusCode());
          response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
          break;
        default:
          throw new ODataApplicationException("Return type not supported",
              HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT);
        }    
    }       
  }
}

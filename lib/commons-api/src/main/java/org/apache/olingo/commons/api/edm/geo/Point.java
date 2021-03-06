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
package org.apache.olingo.commons.api.edm.geo;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;

public class Point extends Geospatial {

  private static final long serialVersionUID = 4917380107331557828L;

  /**
   * The X coordinate of the point. In most long/lat systems, this is the longitude.
   */
  private double x;

  /**
   * The Y coordinate of the point. In most long/lat systems, this is the latitude.
   */
  private double y;

  /**
   * The Z coordinate of the point. In most long/lat systems, this is a radius from the center of the earth, or the
   * height / elevation over the ground.
   */
  private double z;

  public Point(final Dimension dimension, final SRID srid) {
    super(dimension, Type.POINT, srid);
  }

  public double getX() {
    return x;
  }

  public void setX(final double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(final double y) {
    this.y = y;
  }

  public double getZ() {
    return z;
  }

  public void setZ(final double z) {
    this.z = z;
  }

  @Override
  public EdmPrimitiveTypeKind getEdmPrimitiveTypeKind() {
    return dimension == Dimension.GEOGRAPHY
        ? EdmPrimitiveTypeKind.GeographyPoint
        : EdmPrimitiveTypeKind.GeometryPoint;
  }
}

/*
 *
 *  * Copyright (c) 2014- MHISoft LLC and/or its affiliates. All rights reserved.
 *  * Licensed to MHISoft LLC under one or more contributor
 *  * license agreements. See the NOTICE file distributed with
 *  * this work for additional information regarding copyright
 *  * ownership. MHISoft LLC licenses this file to you under
 *  * the Apache License, Version 2.0 (the "License"); you may
 *  * not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package org.mhisoft.common.zip.impl;

/**
 * Copy&Paste of constants needed from java.util.ZipConstants
 * as this "baseinterface" prevents reuse by its package only
 * visibility.
 *
 * @author olaf@merkert.de
 */
public interface ZipConstants {

  /*
   * Header signatures
   */
  static long LOCSIG = 0x04034b50L; // "PK\003\004"
  static long EXTSIG = 0x08074b50L; // "PK\007\008"
  static long CENSIG = 0x02014b50L; // "PK\001\002"
  static long ENDSIG = 0x06054b50L; // "PK\005\006"

}

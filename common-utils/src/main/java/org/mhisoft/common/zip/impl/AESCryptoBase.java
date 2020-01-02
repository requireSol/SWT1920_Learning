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
 * Base class for crypto "adapters" to support aes operations
 * needed for winzip aes.
 *
 * @author olaf@merkert.de
 */
public class AESCryptoBase {

	public static final int KEY_SIZE_BIT = 256;

	public static final int KEY_SIZE_BYTE = KEY_SIZE_BIT / 8;

	public static final int ITERATION_COUNT = 1000;

	// --------------------------------------------------------------------------

	protected byte[] saltBytes;

	protected byte[] cryptoKeyBytes;

	protected byte[] authenticationCodeBytes;

	protected byte[] pwVerificationBytes;

	protected int blockSize;

	protected int nonce;

}

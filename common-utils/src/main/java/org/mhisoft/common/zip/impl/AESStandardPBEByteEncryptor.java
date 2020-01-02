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

import java.util.Arrays;
import java.util.zip.ZipException;
import java.io.IOException;

import org.mhisoft.common.util.security.PBEEncryptor;

/**
 * Description:
 *
 * @author Tony Xue
 * @since Jun, 2016
 */
public class AESStandardPBEByteEncryptor implements AESEncrypter, CiperParam {

	//public static final int keySize = 256;

	private PBEEncryptor encryptor;
	private byte[] cipherParameters;

	@Override
	public void init(String pwStr, int keySize) throws ZipException {
		encryptor = new PBEEncryptor(pwStr);
	}

	@Override
	public byte[] encrypt(byte[] in, int length) {
		try {
			this.cipherParameters = null;
			PBEEncryptor.EncryptionResult ret = encryptor.encrypt(in);
			this.cipherParameters = encryptor.getCipherParameters();
			return ret.getEncryptedData();
		} catch (IOException e) {
			throw new RuntimeException("getCipherParameters() failed", e);
		}

	}

	@Override
	public byte[] getSalt() {
		return null;
	}


	/**
	 * Call this only after the encrypt() method because each encrypt methods will change the
	 * Cipher parameters.
	 *
	 * @return
	 */
	@Override
	public byte[] getCipherParameters() {
		return this.cipherParameters;
	}


	//two bytes
	@Override
	public byte[] getPwVerification() {
		byte[] passwordVerifier = new byte[2];
		Arrays.fill(passwordVerifier, (byte) 0x10);
		return passwordVerifier;
	}

	@Override
	public byte[] getFinalAuthentication() {
		//dummy
		byte[] auth = new byte[10];
		Arrays.fill(auth, (byte) 0x10);
		return auth;
	}

	@Override
	public int getSaltOrCiperParameterLength() throws IOException {
		if (encryptor.getCipherParameters() == null)
			return 100;
		return encryptor.getCipherParameters().length; //100
	}


}

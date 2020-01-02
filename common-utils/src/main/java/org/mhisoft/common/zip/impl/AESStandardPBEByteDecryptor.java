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
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.mhisoft.common.util.security.PBEEncryptor;

/**
 * Description:
 *
 * @author Tony Xue
 * @since Jun, 2016
 */
public class AESStandardPBEByteDecryptor implements AESDecrypter, CiperParam {
	PBEEncryptor encryptor;

	@Override
	public void init(String pwStr, int keySize, byte[] saltOrCiperParameter, byte[] pwVerification) throws ZipException {
		this.encryptor = new PBEEncryptor(pwStr);
	}

	public int getSaltOrCiperParameterLength() throws IOException {
//		if (algorithmParameters==null)
//			return 104;
//		return algorithmParameters.getEncoded().length; //100
		return 100;
	}

	@Override
	public byte[] decrypt(byte[] in, int length, byte[] ciperParams)  throws EncryptionOperationNotPossibleException {
		try {
			AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance(PBEEncryptor.ALGORITHM);
			algorithmParameters.init(ciperParams);
			return encryptor.decrypt(in, algorithmParameters);
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] getFinalAuthentication() {
		//dummy
		byte[] auth = new byte[10];
		Arrays.fill(auth, (byte) 0x10);
		return auth;
	}
}

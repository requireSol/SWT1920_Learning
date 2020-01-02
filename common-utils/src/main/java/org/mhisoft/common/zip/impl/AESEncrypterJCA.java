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

import java.util.Random;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * Encrypter adapter for the Java Cryptography Architecture.
 *
 * @author Matthew Dempsky <mdempsky@google.com>
 */
public class AESEncrypterJCA implements AESEncrypter, CiperParam {

	private byte[] salt;
	private AESUtilsJCA utils;

	public void init(String password, int keySize) {
		salt = createSalt(keySize / 16);
		utils = new AESUtilsJCA(password, keySize, salt);
	}

	@Override
	public byte[] encrypt(byte[] in, int length) {
		utils.cryptUpdate(in, length);
		utils.authUpdate(in, length);
		return in;
	}

	@Override
	public byte[] getSalt() {
		return salt;
	}

	@Override
	public byte[] getPwVerification() {
		return utils.getPasswordVerifier();
	}

	@Override
	public byte[] getFinalAuthentication() {
		return utils.getFinalAuthentifier();
	}

	private static final Random RANDOM = new SecureRandom();

	private static byte[] createSalt(int size) {
		byte[] salt = new byte[size];
		RANDOM.nextBytes(salt);
		return salt;
	}


	@Override
	public byte[] getCipherParameters() {
		return null;
	}

	public int getSaltOrCiperParameterLength() throws IOException {
		return salt.length;
	}
}

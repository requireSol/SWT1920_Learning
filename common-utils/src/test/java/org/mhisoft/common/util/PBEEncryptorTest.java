
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

package org.mhisoft.common.util;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;

import org.junit.Assert;
import org.junit.Test;
import org.mhisoft.common.util.security.PBEEncryptor;


/**
 * Description:
 *
 * @author Tony Xue
 * @since Mar, 2016
 */
public class PBEEncryptorTest {
	static int FIXED_RECORD_LENGTH=2000;
	@Test
	public void testPBEEncryption() {
		try {
			PBEEncryptor encryptor = new PBEEncryptor("testpassword2343");

			String s1 = "1.FooBar 23974034 &&^23 时尚 ~!)\\u";
			String s2 = "2.FooBar 2397 时尚 ~!)\\u";
			PBEEncryptor.EncryptionResult ret = encryptor.encrypt(StringUtils.getBytes(s1));
			byte[] enc = ret.getEncryptedData();

			System.out.println(StringUtils.toHexString(enc));

			byte[] byteItem = FileUtils.padByteArray(enc, FIXED_RECORD_LENGTH);
			byte[] enc1_2 = FileUtils.trimByteArray(byteItem);

			byte[] params = ret.getCipherParameters();


			//decrypt using a new instance of encryptor.
			AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance(PBEEncryptor.ALGORITHM);
			algorithmParameters.init(params);


			PBEEncryptor encryptor2 = new PBEEncryptor("testpassword2343");
			byte[]  dec = encryptor2.decrypt(enc1_2, algorithmParameters);
			System.out.println(StringUtils.bytesToString(dec));

			Assert.assertEquals(s1,  StringUtils.bytesToString(dec));


			//again, salt is changed.
			ret = encryptor.encrypt(StringUtils.getBytes(s2));
			byte[] enc2 = ret.getEncryptedData();

			//decrypt using a new instance of encryptor.
			byte[] params2  = encryptor.getCipherParameters();
			AlgorithmParameters algorithmParameters2 = AlgorithmParameters.getInstance(PBEEncryptor.ALGORITHM);
			algorithmParameters2.init(params2);

			byte[]  dec2 = encryptor2.decrypt(enc2, algorithmParameters2);
			System.out.println(StringUtils.bytesToString(dec2));
			Assert.assertEquals(s2,  StringUtils.bytesToString(dec2));



		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Test
	public void testIterate() {
		String passBase = "test-23*(&r";
		String s2 = "2.FooBar 2397 时尚 ~!)\\u";

		try {
			for (int i = 0; i < 10; i++) {

				long t1 = System.currentTimeMillis();


				PBEEncryptor encryptor = new PBEEncryptor(passBase+i);
				PBEEncryptor.EncryptionResult ret  = encryptor.encrypt( StringUtils.getBytes(s2+ Integer.valueOf(i) )  ) ;
				byte[] encText = ret.getEncryptedData();
				System.out.println(StringUtils.toHexString(encText));

				byte[] params = encryptor.getCipherParameters();
				System.out.println("params:" + StringUtils.toHexString(params));

				//decrypt using a new instance of encryptor.
				AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance(PBEEncryptor.ALGORITHM);
				algorithmParameters.init(params);
				PBEEncryptor decryptor = new PBEEncryptor(passBase+i);
				byte[]  dec = decryptor.decrypt(encText, algorithmParameters);
				System.out.println(StringUtils.bytesToString(dec));

				//you code to be timed here
				long t2 = System.currentTimeMillis();
				System.out.println("iteration, took " + (t2 - t1));

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

}

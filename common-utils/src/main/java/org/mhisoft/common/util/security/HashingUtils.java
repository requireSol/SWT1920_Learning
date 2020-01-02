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

/**
 * see https://github.com/defuse/password-hashing
 */
package org.mhisoft.common.util.security;

import java.util.Base64;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/*
I made the following chagnes:
 1. use PBKDF2WithHmacSHA512 algorithm, also use the whole algorithm in the hash result.
 2. use the JAVA 8 Base64
 3. convert to unit test


 */
public class HashingUtils {

	@SuppressWarnings("serial")
	static public class InvalidHashException extends Exception {
		public InvalidHashException(String message) {
			super(message);
		}
		public InvalidHashException(String message, Throwable source) {
			super(message, source);
		}
	}

	@SuppressWarnings("serial")
	static public class CannotPerformOperationException extends Exception {
		public CannotPerformOperationException(String message) {
			super(message);
		}
		public CannotPerformOperationException(String message, Throwable source) {
			super(message, source);
		}
	}

	//Tony changed:
	public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA512";

	// These constants may be changed without breaking existing hashes.
	public static final int SALT_BYTE_SIZE = 24;
	public static final int HASH_BYTE_SIZE = 36;
	public static final int PBKDF2_ITERATIONS = 80000; //128000;

	// These constants define the encoding and may not be changed.
	public static final int HASH_SECTIONS = 5;
	public static final int HASH_ALGORITHM_INDEX = 0;
	public static final int ITERATION_INDEX = 1;
	public static final int HASH_SIZE_INDEX = 2;
	public static final int SALT_INDEX = 3;
	public static final int PBKDF2_INDEX = 4;

	static SecureRandom random = null;
	final transient  static Object lock = new Object();

	private static SecureRandom getSecureRandom() {
		if (random==null) {
			synchronized (lock) {
				if (random==null) {
					random = new SecureRandom();
					long t1 = System.currentTimeMillis();
					byte[] salt = new byte[SALT_BYTE_SIZE];
					random.nextBytes(salt);
					long t2 = System.currentTimeMillis();
					System.out.println(" random:" + (t2-t1) );
				}
				return random;
			}
		}
		else
			return random;


	}


	public static String createHash(String password)
			throws CannotPerformOperationException
	{
		return createHash(password.toCharArray());
	}

	public static String createHash(char[] password)
			throws CannotPerformOperationException
	{
//		// Generate a random salt

//		SecureRandom random = new SecureRandom();
//		byte[] salt = new byte[SALT_BYTE_SIZE];
//		random.nextBytes(salt);


		SecureRandom random = getSecureRandom();
		byte[] salt = new byte[SALT_BYTE_SIZE];
		random.nextBytes(salt);




		// Hash the password
		byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE);
		int hashSize = hash.length;

		// format: algorithm:iterations:hashSize:salt:hash
		String parts = PBKDF2_ALGORITHM +
				":" + PBKDF2_ITERATIONS +
				":" + hashSize +
				":" + toBase64(salt) +
				":" + toBase64(hash);
		return parts;
	}

	public static boolean verifyPassword(String password, String correctHash)
			throws CannotPerformOperationException, InvalidHashException
	{
		return verifyPassword(password.toCharArray(), correctHash);
	}

	public static boolean verifyPassword(char[] password, String correctHash)
			throws CannotPerformOperationException, InvalidHashException
	{
		// Decode the hash into its parameters
		String[] params = correctHash.split(":");
		if (params.length != HASH_SECTIONS) {
			throw new InvalidHashException(
					"Fields are missing from the password hash."
			);
		}

		// Currently, Java only supports SHA1.
		if (!params[HASH_ALGORITHM_INDEX].equals(PBKDF2_ALGORITHM)) {
			throw new CannotPerformOperationException(
					"Unsupported hash type."
			);
		}

		int iterations = 0;
		try {
			iterations = Integer.parseInt(params[ITERATION_INDEX]);
		} catch (NumberFormatException ex) {
			throw new InvalidHashException(
					"Could not parse the iteration count as an integer.",
					ex
			);
		}

		if (iterations < 1) {
			throw new InvalidHashException(
					"Invalid number of iterations. Must be >= 1."
			);
		}


		byte[] salt = null;
		try {
			salt = fromBase64(params[SALT_INDEX]);
		} catch (IllegalArgumentException ex) {
			throw new InvalidHashException(
					"Base64 decoding of salt failed.",
					ex
			);
		}

		byte[] hash = null;
		try {
			hash = fromBase64(params[PBKDF2_INDEX]);
		} catch (IllegalArgumentException ex) {
			throw new InvalidHashException(
					"Base64 decoding of pbkdf2 output failed.",
					ex
			);
		}


		int storedHashSize = 0;
		try {
			storedHashSize = Integer.parseInt(params[HASH_SIZE_INDEX]);
		} catch (NumberFormatException ex) {
			throw new InvalidHashException(
					"Could not parse the hash size as an integer.",
					ex
			);
		}

		if (storedHashSize != hash.length) {
			throw new InvalidHashException(
					"Hash length doesn't match stored hash length."
			);
		}

		// Compute the hash of the provided password, using the same salt,
		// iteration count, and hash length
		long t1 = System.currentTimeMillis();
		byte[] testHash = pbkdf2(password, salt, iterations, hash.length);
		long t2 = System.currentTimeMillis();
		System.out.println("\tpbkdf2(), took" + (t2-t1));

		// Compare the hashes in constant time. The password is correct if
		// both hashes match.
		boolean b=  slowEquals(hash, testHash);
		long t3 = System.currentTimeMillis();
		System.out.println("\tslowEquals(), took" + (t3-t2));
		return b;
	}

	private static boolean slowEquals(byte[] a, byte[] b)
	{
		int diff = a.length ^ b.length;
		for(int i = 0; i < a.length && i < b.length; i++)
			diff |= a[i] ^ b[i];
		return diff == 0;
	}

	private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
			throws CannotPerformOperationException
	{
		try {
			PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
			return skf.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException ex) {
			throw new CannotPerformOperationException(
					"Hash algorithm not supported.",
					ex
			);
		} catch (InvalidKeySpecException ex) {
			throw new CannotPerformOperationException(
					"Invalid key spec.",
					ex
			);
		}
	}

	private static byte[] fromBase64(String strEncoded) throws IllegalArgumentException
	{
		return Base64.getDecoder().decode( strEncoded );

	}

	private static String toBase64(byte[] array)
	{
		return Base64.getEncoder().encodeToString( array );
	}



	public static void init() {
		Thread t = new Thread( new HashUtilInit() );
		t.setDaemon(true);
		t.start();
	}


}


class HashUtilInit implements  Runnable {
	@Override
	public void run() {
		//call this once to prepare the salt generator.
		//which take time.
		try {
			long t1 = System.currentTimeMillis();
			HashingUtils.createHash("test");
			long t2 = System.currentTimeMillis();
			System.out.println("HashUtilInit completed, took " + (t2-t1));
		} catch (HashingUtils.CannotPerformOperationException e) {
			//e.printStackTrace();
		}
	}
}
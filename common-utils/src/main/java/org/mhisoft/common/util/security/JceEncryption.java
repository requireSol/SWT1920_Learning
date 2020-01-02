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

package org.mhisoft.common.util.security;

import java.util.Base64;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.mhisoft.common.util.StringUtils;

public class JceEncryption implements CipherInputStreamProvider, CipherOutputStreamProvider {

	private static final Logger logger = Logger.getLogger(JceEncryption.class.getName());

	protected static final String DEFAULT_ALGORITHM = "AES"; // 256-bit AES
	private static final int DEFAULT_SALT_LENGTH = 8;
	private static final int DEFAULT_KEYSIZE = 256;
	private static final int DEFAULT_ITERATIONS = 1000;


	private EncryptionConfig config;
	private EncryptionRuntime runtime;
	private static JceEncryption defaultEngine = null;
	private static Object lock = new Object();



	/**
	 * Get a initialized  engine using default settings AES 256 bit key.
	 */
	public static JceEncryption getDefaultEngine() {
		try {
			if (defaultEngine == null) {
				synchronized (lock) {
					if (defaultEngine == null) {
						defaultEngine = new JceEncryption(DEFAULT_ALGORITHM, null);
						defaultEngine.initRuntime();
					}
				}
			}
			return defaultEngine;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}

	}


	/**
	 * Get a initialized  engine using default settings AES 256 bit key.
	 * Every time a new engine is created, a new key is used.
	 */
	public static JceEncryption createEngine() {
		try {
			JceEncryption ret = new JceEncryption(DEFAULT_ALGORITHM, null);
			ret.initRuntime();
			return ret;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}

	}


	/**
	 * Configuration parameters for the encryption - bundled together to treat them atomically as initialized or not
	 */
	protected static class EncryptionConfig {

		final String provider;

		String algorithm = DEFAULT_ALGORITHM;
		/**
		 * The serialized key
		 */
		String key;

		String mode = "CBC";

		String padding = "PKCS5Padding "; //PKCS5Padding

		int keySize = DEFAULT_KEYSIZE;


		void setKey(final String key) {
			this.key = key;
		}

		public EncryptionConfig(final String _provider, final String _algorithm, final String _key,
				final String _mode, final String _padding) throws GeneralSecurityException {
			super();
			this.provider = _provider;
			if (algorithm != null)
				this.algorithm = _algorithm;
			if (_key == null) {
				//throw new GeneralSecurityException("a key is required");
				//k = generateKey(_provider, _algorithm, DEFAULT_KEYSIZE);
			} else {
				this.keySize = DEFAULT_KEYSIZE;
				this.key = _key;
			}
			if (_mode != null)
				this.mode = _mode;
			if (_padding != null)
				this.padding = _padding;
		}
	}


	/**
	 * Runtime parameters for the encryption - bundled together to treat them atomically as initialized or not
	 */
	private static class EncryptionRuntime {
		final transient Key k;
		final transient Cipher encrypter;
		final transient Cipher decrypter;
		transient byte[] iv;
		final EncryptionConfig config;


		public EncryptionRuntime(final EncryptionConfig _config) throws GeneralSecurityException {
			this.config = _config;
			final String fullAlgorithmName = getFullAlgorithmName(config);
			try {
				if (config.provider == null) {
					encrypter = Cipher.getInstance(fullAlgorithmName);
					decrypter = Cipher.getInstance(fullAlgorithmName);
				} else {
					encrypter = Cipher.getInstance(fullAlgorithmName, config.provider);
					decrypter = Cipher.getInstance(fullAlgorithmName, config.provider);
				}
			} catch (final GeneralSecurityException e) {
				throw new GeneralSecurityException("Problem while initializing JceEncryption: provider: " + config.provider + " algorithm: "
						+ fullAlgorithmName, e);
			}


			try {
				/*If you use a block-chaining mode like CBC, you need to provide an IvParameterSpec to the Cipher as well.*/
				k = getKey(config);

				iv = new byte[encrypter.getBlockSize()];    // Save the IV bytes or send it in plaintext with the encrypted data so you can decrypt the data later
				SecureRandom prng = new SecureRandom();
				prng.nextBytes(iv);
				encrypter.init(Cipher.ENCRYPT_MODE, k, new IvParameterSpec(iv));
				decrypter.init(Cipher.DECRYPT_MODE, k, new IvParameterSpec(iv));
				if (config.keySize == 0)
					config.keySize = k.getEncoded().length * 8;
			} catch (final InvalidKeyException e) {
				throw new InvalidKeyException("Invalid key [" + config.key + "] while initializing JceEncryption: provider: " + config.provider
						+ " algorithm: " + fullAlgorithmName, e);
			}
		}


		private Key getKey(final EncryptionConfig config) throws InvalidKeyException, NoSuchAlgorithmException,
				NoSuchProviderException {
			Key result;
			if (config.key == null || config.key.isEmpty()) {
				//generate key
				logger.info("generate a key ");
				result = generateKey(config.provider, config.algorithm, config.keySize);
			} else
				result = deserializeKey(config);
			return result;
		}


		private Key deserializeKey(final EncryptionConfig config) throws InvalidKeyException {
			final Key _key;
			try {
				_key = KeySerializer.instance.deserialize(config.key);
				;
			} catch (IOException | ClassNotFoundException e) {
				throw new InvalidKeyException(e);
			}
			final String keysAlgorithm = _key.getAlgorithm();
			if (!keysAlgorithm.equals(config.algorithm))
				throw new InvalidKeyException("Wrong algorithm for key, expected: " + config.algorithm + ", actual: " + keysAlgorithm);
			return _key;
		}

		private void initIV() throws GeneralSecurityException {
			try {
				iv = new byte[encrypter.getBlockSize()];    // Save the IV bytes or send it in plaintext with the encrypted data so you can decrypt the data later
				SecureRandom prng = new SecureRandom();
				prng.nextBytes(iv);
				encrypter.init(Cipher.ENCRYPT_MODE, k, new IvParameterSpec(iv));
				decrypter.init(Cipher.DECRYPT_MODE, k, new IvParameterSpec(iv));
				if (config.keySize == 0)
					config.keySize = k.getEncoded().length * 8;
			} catch (final InvalidKeyException | InvalidAlgorithmParameterException e) {
				throw new GeneralSecurityException("Invalid key [" + config.key + "] while initializing JceEncryption: provider: " + config.provider
						+ " algorithm: " + config.algorithm, e);
			}
		}

	}   //EncryptionRuntime


	public JceEncryption() {
		//
	}

	@Override
	public String toString() {
		return runtime.encrypter.getProvider() + ":" + runtime.encrypter.getAlgorithm() +
				", blocksize:" + runtime.encrypter.getBlockSize();

	}

	/**
	 * Initialize with these parameters the encryption and decryption engines.
	 *
	 * @throws GeneralSecurityException if something goes wrong initializing.
	 */
	public void init(final String _provider, final String _algorithm, final String _key, final String _mode,
			final String _padding) throws GeneralSecurityException {
		config = new EncryptionConfig(_provider, _algorithm == null ? DEFAULT_ALGORITHM : _algorithm, _key, _mode, _padding);
		initRuntime();

	}


	public JceEncryption(final String key) throws GeneralSecurityException {
		this(null, DEFAULT_ALGORITHM, key, null, null);
	}

	public JceEncryption(final String algorithm, final String key) throws GeneralSecurityException {
		this(null, algorithm, key, null, null);
	}


	public JceEncryption(final String provider, final String algorithm, final String key, final String mode,
			final String padding) throws GeneralSecurityException {
		this();
		init(provider, algorithm, key, mode, padding);
	}


	/**
	 * Initialize (or re-initialize with new parameters) the encryption and decryption engines.
	 *
	 * @throws GeneralSecurityException if something goes wrong initializing.
	 */
	public void initRuntime() throws GeneralSecurityException {
		if (config == null)
			throw new GeneralSecurityException("Encryption configuration parameters not yet set");
		runtime = new EncryptionRuntime(config);
	}


	public String getFullAlgorithmName() {
		if (config == null)
			throw new IllegalStateException("Encryption not yet configured");
		final String result = getFullAlgorithmName(config);
		return result;
	}

	protected static String getFullAlgorithmName(final EncryptionConfig config) {
		final StringBuilder sb = new StringBuilder();
		sb.append(config.algorithm);
		if (config.mode != null && config.mode.length() > 0)
			sb.append("/").append(config.mode);
		if (config.padding != null && config.padding.length() > 0)
			sb.append("/").append(config.padding);
		final String result = sb.toString();
		return result;
	}


	public String getProvider() {
		return config == null ? null : config.provider;
	}


	public String getAlgorithm() {
		return config == null ? null : config.algorithm;
	}


	public String getKey() {
		return config == null ? null : config.key;
	}

	public int getKeySize() {
		return config == null ? 0 : config.keySize;
	}


	public String getMode() {
		return config == null ? null : config.mode;
	}


	public String getPadding() {
		return config == null ? null : config.padding;
	}


	/**
	 * return base 64 encoded.
	 */
	public String encrypt(final String text) throws GeneralSecurityException {
		if (runtime == null || config == null)
			throw new IllegalStateException("Encryption not initialized");
		final String result;
		try {
			final byte[] stringBytes = (text == null ? new byte[0] : text.getBytes(Charset.forName("UTF-8")));
			byte[] encrypted = runtime.encrypter.doFinal(stringBytes);
			result = Base64.getEncoder().encodeToString(encrypted);
		} catch (final GeneralSecurityException e) {
			throw new GeneralSecurityException("Problem encrypting text: " + text, e);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] encrypt(final byte[] bytes) throws GeneralSecurityException {
		if (runtime == null || config == null)
			throw new IllegalStateException("Encryption not initialized");
		final byte[] result;
		try {
			result = runtime.encrypter.doFinal(bytes == null ? new byte[0] : bytes);
		} catch (final GeneralSecurityException e) {
			String bytesToString;
			if (bytes == null) {
				bytesToString = "null";
			} else {
				bytesToString = StringUtils.toHexString(bytes);
				if (bytesToString.length() > 64)
					bytesToString = bytesToString.substring(0, 64) + "... (" + bytes.length + " bytes)";
			}
			throw new GeneralSecurityException("Problem encrypting bytes: " + bytesToString, e);
		}
		return result;
	}


	protected int getSaltLength() {
		if (runtime == null)
			throw new IllegalStateException("Encryption not initialized");
		final int saltLength;
		final int algorithmBlockSize = runtime.encrypter.getBlockSize();
		if (algorithmBlockSize > 0)
			saltLength = algorithmBlockSize;
		else
			saltLength = DEFAULT_SALT_LENGTH;
		return saltLength;
	}


	public String decrypt(final String text) throws GeneralSecurityException {
		if (runtime == null || config == null)
			throw new IllegalStateException("Encryption not initialized");
		final String result;
		if (text == null) {
			result = null;
		} else if (text.length() == 0) {
			result = "";
		} else {
			final byte[] raw;
			raw = Base64.getDecoder().decode(text);
			final byte[] stringBytes;
			stringBytes = runtime.decrypter.doFinal(raw);
			result = StringUtils.bytesToString(stringBytes);
		}
		return result;
	}


	public byte[] decrypt(final byte[] bytes) throws GeneralSecurityException {
		if (runtime == null || config == null)
			throw new IllegalStateException("Encryption not initialized");
		final byte[] result;
		if (bytes == null) {
			result = null;
		} else if (bytes.length == 0) {
			result = new byte[0];
		} else {
			try {
				result = runtime.decrypter.doFinal(bytes);

			} catch (final GeneralSecurityException e) {
				String bytesToString = StringUtils.bytesToString(bytes);
				if (bytesToString.length() > 64)
					bytesToString = bytesToString.substring(0, 64) + "... (" + bytes.length + " bytes)";
				throw new GeneralSecurityException("Problem decrypting bytes: " + bytesToString, e);
			}
		}
		return result;
	}


	@Override
	public CipherOutputStream getCipherOutputStream(final OutputStream os) {
		if (runtime == null)
			throw new IllegalStateException("Encryption not initialized");
		return new CipherOutputStream(os, runtime.encrypter);
	}

	@Override
	public CipherInputStream getCipherInputStream(final InputStream is) {
		if (runtime == null)
			throw new IllegalStateException("Encryption not initialized");
		return new CipherInputStream(is, runtime.decrypter);
	}

	public static SecretKey generateKey() throws NoSuchAlgorithmException, NoSuchProviderException {
		return generateKey(null, DEFAULT_ALGORITHM, DEFAULT_KEYSIZE);
	}


	public static SecretKey generateKey(String provider, String algorithm, int keysize) throws NoSuchAlgorithmException,
			NoSuchProviderException {
		KeyGenerator keyGenerator;
		if (provider == null)
			keyGenerator = KeyGenerator.getInstance(algorithm);
		else
			keyGenerator = KeyGenerator.getInstance(algorithm, provider);
		if (keysize > 0) // if keysize > 0 then a non-default keysize is requested, use the requested size
			keyGenerator.init(keysize);
		final SecretKey result = keyGenerator.generateKey();
		return result;
	}

}

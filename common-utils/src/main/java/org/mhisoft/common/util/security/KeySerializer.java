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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;

public class KeySerializer {

	public static KeySerializer instance = new KeySerializer();

	private KeySerializer() {
		//
	}

	/**
	 * Serialize a Key into a string.
	 * @param key the key to be stored in a string.
	 * @return the String representation of this key.
	 */
	public String serialize(final Key key) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(key);
			oos.close();
			oos = null;
		} catch (final IOException e) {
			// can't happen
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (final IOException e) {
					// ignore
				}
				oos = null;
			}
		}
		final byte[] resultBytes = baos.toByteArray();
		final String result = Base64.getEncoder().encodeToString(resultBytes);
		return result;
	}

	/**
	 * Deserialized a key stored in a string into a Key.
	 * @param serializedKey the key stored in a string
	 * @return the Key
	 * @throws ClassNotFoundException if the deserialization fails because the serialized key's class is not available in the classloader.
	 */
	public Key deserialize(final String serializedKey) throws  IOException, ClassNotFoundException {
		Key result;
		final byte[] b = Base64.getDecoder().decode(serializedKey);
		final ByteArrayInputStream bais = new ByteArrayInputStream(b);
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(bais);
			result = (Key) ois.readObject();
			ois.close();
			ois = null;
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (final IOException e) {
					// ignore
				}
				ois = null;
			}
		}
		return result;
	}
}

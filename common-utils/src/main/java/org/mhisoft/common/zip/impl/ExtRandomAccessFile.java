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
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static org.mhisoft.common.util.ByteArrayHelper.toLong;
import static org.mhisoft.common.util.ByteArrayHelper.toShort;
import static org.mhisoft.common.util.ByteArrayHelper.toInt;


/**
 * direct access methods accepting position + type of data to read as args
 * 
 * @author olaf@merkert.de
 */
public class ExtRandomAccessFile {

	protected RandomAccessFile file;

	public ExtRandomAccessFile(File zipFile) throws IOException {
		this.file = new RandomAccessFile(zipFile, "r");
	}

	public void close() throws IOException {
		file.close();
	}

	// --------------------------------------------------------------------------

	public int readByteArray(byte[] buffer, int len) throws IOException {
		int read = file.read(buffer, 0, len);
		return read;
	}

	public byte[] readByteArray(long pos, int length) throws IOException {
		byte[] out = new byte[length];
		file.seek(pos);
		file.read(out, 0, length);
		return out;
	}

	public long readLong() throws IOException {
		byte[] b = new byte[8];
		file.read(b, 0, 8);
		long out = toLong(b);
		return out;
	}

	public long readLong(long pos) throws IOException {
		file.seek(pos);
		return readLong();
	}

	public int readInt() throws IOException {
		byte[] b = new byte[4];
		file.read(b, 0, 4);
		int out = toInt(b);
		return out;
	}

	public int readInt(long pos) throws IOException {
		file.seek(pos);
		return readInt();
	}

	public short readShort() throws IOException {
		byte[] b = new byte[2];
		file.read(b, 0, 2);
		short out = toShort(b);
		return out;
	}

	public short readShort(long pos) throws IOException {
		file.seek(pos);
		return readShort();
	}

	public byte readByte() throws IOException {
		byte[] b = new byte[1];
		file.read(b, 0, 1);
		return b[0];
	}
	
	public byte readByte(long pos) throws IOException {
		file.seek(pos);
		return readByte();
	}

	// --------------------------------------------------------------------------

	public void seek(long pos) throws IOException {
		file.seek(pos);
	}

	public long getFilePointer() throws IOException {
		return file.getFilePointer();
	}

	// --------------------------------------------------------------------------

	// TODO implement a buffered version
	public long lastPosOf(byte[] bytesToFind) throws IOException {
		long out = -1;
		for( long seekPos=file.length()-1-bytesToFind.length; seekPos>3 && out==-1; seekPos-- ) {
			byte[] buffer = readByteArray(seekPos,bytesToFind.length);
			if( Arrays.equals(bytesToFind,buffer) ) {
				out = seekPos;
			}
		}
		return out;
	}
	
}

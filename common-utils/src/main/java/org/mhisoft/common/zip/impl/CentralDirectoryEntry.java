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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import java.io.IOException;

import org.mhisoft.common.zip.api.AesZipFileDecrypter;


/**
 * Wrapper for the central directory entry (CDE) of one file.
 * At the end of the ZIP file one CDE can be found for each
 * entry (file) within the zip file.
 * <p>
 * central file header signature   4 bytes  (0x02014b50)
 * version made by                 2 bytes	//	4
 * version needed to extract       2 bytes //	6
 * general purpose bit flag        2 bytes //	8
 * compression method              2 bytes // 10
 * last mod file time              2 bytes	// 12
 * last mod file date              2 bytes	// 14
 * crc-32                          4 bytes	// 16
 * compressed size                 4 bytes	// 20
 * uncompressed size               4 bytes	// 24
 * file name length                2 bytes // 28
 * extra field length              2 bytes	// 30
 * file comment length             2 bytes // 32
 * disk number start               2 bytes // 34
 * internal file attributes        2 bytes // 36
 * external file attributes        4 bytes // 38
 * relative offset of local header 4 bytes // 42
 * <p>
 * file name (variable size)				// 46
 * extra field (variable size)				// 46 + fileNameLength
 * file comment (variable size)			// 46 + fileNameLength + extraFieldLength
 */
public class CentralDirectoryEntry implements ZipConstants {

	private static final Logger LOG = Logger.getLogger(CentralDirectoryEntry.class.getName());

	// ------------------------------------------------------------------------

	protected ExtRandomAccessFile raFile;

	protected long fileOffset;

	protected boolean isEncrypted;

	protected boolean isAesEncrypted;

	protected short fileNameLength;

	protected long extraFieldOffset;

	protected String fileName;

	protected int localHeaderSize;

	protected short actualCompressionMethod;

	protected short extraFieldLength;

	protected long localHeaderOffset;

	protected int compressedSize;

	protected int uncompressedSize;

	// ------------------------------------------------------------------------

	public CentralDirectoryEntry(ExtRandomAccessFile raFile, long fileOffset) throws IOException {
		this.raFile = raFile;
		this.fileOffset = fileOffset;
		initFromRaFile();
	}

	protected void initFromRaFile() throws IOException {
		// Central directory structure / central file header signature
		int censig = raFile.readInt(fileOffset);
		if (censig != CENSIG) {
			throw new ZipException("expected CENSIC not found in central directory (at end of zip file)");
		} else if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("found censigOffset=" + fileOffset);
		}

		short flag = raFile.readShort(fileOffset + 8);
		this.isEncrypted = (flag & 1) > 0;

		this.fileNameLength = raFile.readShort(fileOffset + 28);
		byte[] fileNameBytes = raFile.readByteArray(fileOffset + 46, fileNameLength);
		this.fileName = new String(fileNameBytes, AesZipFileDecrypter.charset);
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("fileName = " + this.fileName);
		}

		this.extraFieldOffset = this.fileOffset + 46 + this.fileNameLength;
		this.extraFieldLength = raFile.readShort(fileOffset + 30);
		this.localHeaderOffset = raFile.readInt(fileOffset + 28 + 14);

		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("CDS - extraFieldOffset =" + Long.toHexString(this.extraFieldOffset));
			LOG.fine("CDS - extraFieldLength =" + this.extraFieldLength);
			LOG.fine("CDS - localHeaderOffset=" + Long.toHexString(this.localHeaderOffset));
		}

		// TODO - check, why we have to use the local header instead of the CDS sometimes...

		if (this.isEncrypted) {
			byte[] efhid = raFile.readByteArray(this.extraFieldOffset, 2);
			if (efhid[0] != 0x01 || efhid[1] != (byte) 0x99) {
				this.extraFieldOffset = localHeaderOffset + 30 + fileNameLength;
				this.extraFieldLength = raFile.readShort(localHeaderOffset + 28);
				if (LOG.isLoggable(Level.FINE)) {
					LOG.fine("local header - extraFieldOffset=" + Long.toHexString(this.extraFieldOffset));
					LOG.fine("local header - extraFieldLength=" + Long.toHexString(this.extraFieldLength));
				}
				if (0 == extraFieldLength) {
					throw new ZipException("extra field is of length 0 - this is probably not a WinZip AES encrypted entry");
				}
				efhid = raFile.readByteArray(extraFieldOffset, 2);
				if (efhid[0] == 0x01 && efhid[1] == (byte) 0x99) {
					this.isAesEncrypted = true;
				}
			} else {
				this.isAesEncrypted = true;
			}

			if (this.isAesEncrypted) {
				this.actualCompressionMethod = raFile.readShort(getExtraFieldOffset() + 9);
				this.localHeaderSize = 30 + getExtraFieldLength() + getFileNameLength();
			}
		}

		this.compressedSize = (int) raFile.readLong(fileOffset + 20);

		this.uncompressedSize = (int) raFile.readLong(fileOffset + 24);

	}

	// ------------------------------------------------------------------------

	public int getCompressedSize() {
		return this.compressedSize;
	}

	public int getUncompressedSize() {
		return this.uncompressedSize;
	}

	public boolean isEncrypted() {
		return isEncrypted;
	}

	public String getFileName() {
		return this.fileName;
	}

	public short getFileNameLength() {
		return fileNameLength;
	}

	public short getExtraFieldLength() {
		return extraFieldLength;
	}

	public long getLocalHeaderOffset() {
		return localHeaderOffset;
	}

	protected long getExtraFieldOffset() {
		return extraFieldOffset;
	}

	/**
	 * @return position within zip file where the actual data of the entry (file) starts
	 * (after encryption salt + pw data)
	 */
	public int getOffset( CiperParam ciperParam) {
		return (int) (getLocalHeaderOffset() + getLocalHeaderSize() + getCryptoHeaderLengthBeforeData(ciperParam));
	}

	/**
	 * (for encrypted files) stored in extra field
	 * java zip only supports STORED and DEFLATED
	 *
	 * @return 0=stored (no compression) | 8=deflated
	 */
	public short getActualCompressionMethod() {
		return actualCompressionMethod;
	}

	/**
	 * this library currently only supports 256bit keys
	 *
	 * @return keySize - 1=128bit | 2=192bit | 3=256bit
	 */
	public byte getEncryptionStrength() throws IOException {
		return raFile.readByte(getExtraFieldOffset() + 8);
	}

	public int getLocalHeaderSize() {
		return localHeaderSize;
	}


	public static  int  getHeaderSaltOrCiperParameterLength(CiperParam ciperParam) {
		try {
			return 4 + ciperParam.getSaltOrCiperParameterLength();  //16,104
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * fixed 256 key size, 16 bytes salt + 2 bytes pwVerification
	 */
	public static int  getCryptoHeaderLengthBeforeData(CiperParam ciperParam) {
		// TODO support 128+192 byte keys reduces the salt byte size to 8+2 or 12+2
		int cryptoHeaderLength = getHeaderSaltOrCiperParameterLength(ciperParam) + 2;
		return cryptoHeaderLength;
		//return 18+4;
	}

	public static int  getCryptoHeaderTotalLength(CiperParam ciperParam) {
		  return getCryptoHeaderLengthBeforeData(ciperParam)+ 10; //add authentication code 10 bytes
	}


	public boolean isAesEncrypted() {
		return isAesEncrypted;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("fileName\t\t = ").append(this.fileName).append('\n');
		try {
			sb.append("uncompressedSize\t = ").append(this.getUncompressedSize()).append('\n');
			sb.append("compressedSize\t\t = ").append(this.getCompressedSize()).append('\n');
			sb.append("encryptionStrength\t = ").append(this.getEncryptionStrength()).append('\n');
			sb.append("extraFieldOffset\t = ").append(this.getExtraFieldOffset()).append('\n');
			sb.append("extraFieldLength\t = ").append(this.getExtraFieldLength()).append('\n');
			sb.append("localHeaderOffset\t = ").append(this.getLocalHeaderOffset()).append('\n');
			sb.append("localHeaderSize\t\t = ").append(this.getLocalHeaderSize()).append('\n');
			//	sb.append( "offset\t\t\t = ").append( this.getOffset() ).append('\n');
			//sb.append().append().append('\n');
		} catch (IOException ioEx) {
			LOG.log(Level.WARNING, ioEx.getMessage(), ioEx);
		}
		return sb.toString();
	}

}

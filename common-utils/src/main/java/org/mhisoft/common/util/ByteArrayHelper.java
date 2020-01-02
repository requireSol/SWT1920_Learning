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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * byte[] functionality
 *
 * @author olaf@merkert.de
 */
public class ByteArrayHelper {

  public static long toLong(byte[] in) {
    long out = 0;
    for( int i=in.length-1; i>0; i-- ) {
      out |= in[i] & 0xff;
      out <<= 8;
    }
    out |= in[0] & 0xff;
    return out;
  }

  public static int toInt(byte[] in) {
    int out = 0;
    for( int i=in.length-1; i>0; i-- ) {
      out |= in[i] & 0xff;
      out <<= 8;
    }
    out |= in[0] & 0xff;
    return out;
  }

  public static short toShort(byte[] in) {
    short out = 0;
    for( int i=in.length-1; i>0; i-- ) {
      out |= in[i] & 0xff;
      out <<= 8;
    }
    out |= in[0] & 0xff;
    return out;
  }

	public static byte[] toByteArray(int in) {
		byte[] out = new byte[4];

		out[0] = (byte)in;
		out[1] = (byte)(in >> 8);
		out[2] = (byte)(in >> 16);
		out[3] = (byte)(in >> 24);

		return out;
	}

	public static byte[] toByteArray(int in,int outSize) {
		byte[] out = new byte[outSize];
		byte[] intArray = toByteArray(in);
		for( int i=0; i<intArray.length && i<outSize; i++ ) {
			out[i] = intArray[i];
		}
		return out;
	}

	public static String toString( byte[] theByteArray ){
		StringBuffer out = new StringBuffer();
		for( int i=0; i<theByteArray.length; i++ ) {
			String s = Integer.toHexString(theByteArray[i]&0xff);
			if( s.length()<2 ) {
				out.append( '0' );
			}
			out.append( s ).append(' ');
		}
		return out.toString();
	}

	public static boolean isEqual( byte[] first, byte[] second ) {
		boolean out = first!=null && second!=null && first.length==second.length;
		for( int i=0; out && i<first.length; i++ ) {
			if( first[i]!=second[i] ) {
				out = false;
			}
		}
		return out;
	}



	public static int bytesToInt(byte[] b) {
		final ByteBuffer bb = ByteBuffer.wrap(b);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getInt();
	}

	public static byte[] intToBytes(int i) {
		final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);   //4 bytes
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(i);
		return bb.array();
	}

	public static byte[] longToBytes(long l) {
		byte[] result = new byte[8];
		for (int i = 7; i >= 0; i--) {
			result[i] = (byte)(l & 0xFF);
			l >>= 8;
		}
		return result;
	}

	public static long bytesToLong(byte[] b) {
		long result = 0;
		for (int i = 0; i < 8; i++) {
			result <<= 8;
			result |= (b[i] & 0xFF);
		}
		return result;
	}


}

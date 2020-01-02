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

import java.util.List;
import java.util.zip.GZIPInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Description:
 *
 * @author Tony Xue
 * @since Dec, 2017
 */
public class CompressionUtilTest {


	@Test
	public void testGetCompressedStream() throws IOException {
		String input = "abcdefghij";
		InputStream sourceStream = new ByteArrayInputStream(input.getBytes());
		InputStream compressedStream =
				CompressionUtil.getCompressedStream(sourceStream, CompressionUtil.CompressionType.GZIP);

		GZIPInputStream decompressedStream = new GZIPInputStream(compressedStream);
		List<String> lines = IOUtils.readLines(decompressedStream);
		String output = lines.get(0);
		Assert.assertEquals(input, output);

	}

	@Test
	public void testGetCompressedBytes() throws IOException {
		String input = "abcdefghij0000000000000000000000000000000000000000000000000000000000000000000000" +
				"rrrrrrr0000000000000000000000000000000000000000000000000000000000000000";
		InputStream sourceStream = new ByteArrayInputStream(input.getBytes());
		byte[] compressedBytes= CompressionUtil.getCompressedBytes(sourceStream);
		System.out.println("before size:" + StringUtils.getBytes(input).length);
		System.out.println("after Compressed size:" + compressedBytes.length);


		//validate the compressed bytes by deflate it and compare with the original text.
		ByteArrayInputStream in = new ByteArrayInputStream(compressedBytes);

		GZIPInputStream decompressedStream = new GZIPInputStream(in);
		List<String> lines = IOUtils.readLines(decompressedStream);
		String output = lines.get(0);
		Assert.assertEquals(input, output);

	}
}

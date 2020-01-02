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

import org.junit.Assert;
import org.junit.Test;

/**
 * Description:
 *
 * @author Tony Xue
 * @since Mar, 2017
 */
public class ByteArrayHelperTest {
	@Test
	public void testLong() {
		for (long i=1;  i<74058508l; i=i+5 ) {
			byte[] b = ByteArrayHelper.longToBytes(i);
			Assert.assertEquals(ByteArrayHelper.bytesToLong(b), i);
		}
	}

	@Test
	public void testInt() {
		for (int i=1;  i<74058508; i=i+1 ) {
			byte[] b = ByteArrayHelper.intToBytes(i);
			Assert.assertEquals(ByteArrayHelper.bytesToInt(b), i);
		}
	}
}

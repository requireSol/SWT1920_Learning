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
 *  *    http:/www.apache.org/licenses/LICENSE-2.0
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

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

/**
 * Description:
 *
 * @author Tony Xue
 * @since Jul, 2017
 */
public class FileUtilsTest {

	String separator = File.separator.equals("\\") ? "\\\\" :File.separator;

	@Test
	public void getFileNameWithoutPath_fileWOExtension() {
		String s = "S:/projects/mhisoft/evault-app/LICENSE";

		s= s.replaceAll("/", separator );
		Assert.assertEquals("LICENSE", FileUtils.getFileNameWithoutPath(s));
	}

	@Test
	public void getFileNameWithoutPath() {
		String s = "S:/projects/mhisoft/evault-app/dist/test.docx";
		s = s.replaceAll("/", separator);
		Assert.assertEquals("test.docx", FileUtils.getFileNameWithoutPath(s));

		String s3 = "S:/projects/mhisoft/evault-app/";
		s3 = s3.replaceAll("/", separator);
		Assert.assertNull(FileUtils.getFileNameWithoutPath(s3));
	}

	@Test
	public void getFileDir() {
		String dir = "S:/projects/mhisoft/evault-app/dist".replaceAll("/", separator);

		String s = dir + File.separator + "test.docx";
		Assert.assertEquals(dir, FileUtils.gerFileDir(s));

		String s2 =  dir;
		Assert.assertEquals("S:/projects/mhisoft/evault-app".replaceAll("/", separator), FileUtils.gerFileDir(s2));

		String s3 = "S:/projects/mhisoft/evault-app/";
		s3=s3.replaceAll("/", separator);
		Assert.assertEquals("S:/projects/mhisoft/evault-app".replaceAll("/",separator),  FileUtils.gerFileDir(s3));
	}


	@Test
	public void getFileDirRelative() {
		String s3 = "./target/classes/AttachmentServiceTest.dat";
		s3=s3.replaceAll("/", separator);
		Assert.assertEquals("./target/classes".replaceAll("/",separator), FileUtils.gerFileDir(s3));

	}
}

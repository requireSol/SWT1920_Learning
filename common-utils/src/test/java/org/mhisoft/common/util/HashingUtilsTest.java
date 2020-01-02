/**
 * see https://github.com/defuse/password-hashing/tree/master/tests
 */

/*
 * Copyright (c) 2016, Taylor Hornby
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation and/or
 other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mhisoft.common.util;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mhisoft.common.util.security.HashingUtils;


public class HashingUtilsTest
{

	@BeforeClass
	public static void setup() {
		HashingUtils.init();
	}


	// Make sure truncated hashes don't validate.
	@Test
	public  void truncatedHashTest() {
		String userString = "password!";
		String goodHash = "";
		String badHash = "";
		int badHashLength = 0;

		try {
			goodHash = HashingUtils.createHash(userString);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
		badHashLength = goodHash.length();

		do {
			badHashLength -= 1;
			badHash = goodHash.substring(0, badHashLength);

			boolean raised = false;
			try {
				HashingUtils.verifyPassword(userString, badHash);
			} catch (HashingUtils.InvalidHashException ex) {
				raised = true;
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
				System.exit(1);
			}

			if (!raised) {
				System.out.println("Truncated hash test: FAIL " +
						"(At hash length of " +
						badHashLength + ")"
				);
				Assert.fail();
				System.exit(1);
			}

			// The loop goes on until it is two characters away from the last : it
			// finds. This is because the PBKDF2 function requires a hash that's at
			// least 2 characters long.
		} while (badHash.charAt(badHashLength - 3) != ':');

		System.out.println("Truncated hash test: pass");
	}

	/**
	 * Tests the basic functionality of the HashingUtils class
	 *
	 */
	@Test
	public  void basicTests()
	{
		try
		{
			// Test password validation
			boolean failure = false;
			for(int i = 0; i < 10; i++)
			{
				String password = ""+i;
				long t1 = System.currentTimeMillis();
				String hash = HashingUtils.createHash(password);
				String secondHash = HashingUtils.createHash(password);
				System.out.println(hash+"<-->"+secondHash);
				if(hash.equals(secondHash)) {
					System.out.println("FAILURE: TWO HASHES ARE EQUAL!");
					failure = true;
				}
				String wrongPassword = ""+(i+1);

				long t2 = System.currentTimeMillis();
				System.out.println("\t\t createHash took "+ (t2-t1));

				t1 = System.currentTimeMillis();
				if(HashingUtils.verifyPassword(wrongPassword, hash)) {
					System.out.println("FAILURE: WRONG PASSWORD ACCEPTED!");
					failure = true;
				}
				t2 = System.currentTimeMillis();
				System.out.println("\t\t verifyPassword took "+ (t2-t1));


				t1 = System.currentTimeMillis();
				if(!HashingUtils.verifyPassword(password, hash)) {
					System.out.println("FAILURE: GOOD PASSWORD NOT ACCEPTED!");
					failure = true;
				}
				t2 = System.currentTimeMillis();
				System.out.println("\t\t verifyPassword took "+ (t2-t1));
			}
			if(failure) {
				System.out.println("TESTS FAILED!");
				Assert.fail();
				System.exit(1);
			}
		}
		catch(Exception ex)
		{
			Assert.fail();
			System.out.println("ERROR: " + ex);
			System.exit(1);
		}
	}

	@Test
	public  void testHashFunctionChecking()
	{
		try {
			String hash = HashingUtils.createHash("foobar");
			hash = hash.replaceFirst(HashingUtils.PBKDF2_ALGORITHM+":", "sha256:");

			boolean raised = false;
			try {
				HashingUtils.verifyPassword("foobar", hash);
			} catch (HashingUtils.CannotPerformOperationException ex) {
				raised = true;
			}

			if (raised) {
				System.out.println("Algorithm swap: pass");
			} else {
				System.out.println("Algorithm swap: FAIL");
				Assert.fail();
				System.exit(1);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			Assert.fail();
			System.exit(1);
		}

	}


	@Test
	public  void  testCreateHash()
	{
		try
		{
			// Test password validation
			boolean failure = false;
			for(int i = 0; i < 10; i++)
			{
				String password = "abcABC!" + i;
				long t1 = System.currentTimeMillis();
				String hash = HashingUtils.createHash(password);
				System.out.println("pass:" + password +", hash=" + hash );

				long t2 = System.currentTimeMillis();
				System.out.println("\t\t createHash took "+ (t2-t1));

				HashingUtils.verifyPassword(password, hash );
				long t3 = System.currentTimeMillis();
				System.out.println("\t\t verify took "+ (t3-t1));


			}
			if(failure) {
				System.out.println("TESTS FAILED!");
				Assert.fail();
				System.exit(1);
			}
		}
		catch(Exception ex)
		{
			Assert.fail();
			System.out.println("ERROR: " + ex);
			System.exit(1);
		}
	}
}
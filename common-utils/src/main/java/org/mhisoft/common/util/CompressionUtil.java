package org.mhisoft.common.util;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.commons.io.IOUtils;

/**
 * Stream Compression Utility
 *
 * @author Thamme Gowda N
 */
public enum CompressionUtil {
	INSTANCE;

	public static final int NUM_THREADS = 5;
	private final ExecutorService pool;

	CompressionUtil(){
		this.pool = Executors.newFixedThreadPool(NUM_THREADS);
	}

	public static CompressionUtil getInstance(){
		return INSTANCE;
	}

	/**
	 * Supported compression type names
	 */
	public static enum CompressionType {
		GZIP,
		ZIP
	}

	/**
	 * Wraps the given stream in a Compressor stream based on given type
	 * @param sourceStream : Stream to be wrapped
	 * @param type         : Compression type
	 * @return source stream wrapped in a compressor stream
	 * @throws IOException when some thing bad happens
	 */
	public static OutputStream getCompressionWrapper(OutputStream sourceStream,
			CompressionType type) throws IOException {

		switch (type) {
			case GZIP:
				return new GZIPOutputStream(sourceStream);
			case ZIP:
				return new ZipOutputStream(sourceStream);
			default:
				throw new IllegalArgumentException("Possible values :"
						+ Arrays.toString(CompressionType.values()));
		}
	}

	/**
	 * Gets Compressed Stream for given input Stream
	 * @param sourceStream  : Input Stream to be compressed to
	 * @param type: Compression types such as GZIP
	 * @return  Compressed Stream
	 * @throws IOException when some thing bad happens
	 */
	public static InputStream getCompressedStream(final InputStream sourceStream,
			CompressionType type ) throws IOException {

		if(sourceStream == null) {
			throw new IllegalArgumentException("Source Stream cannot be NULL");
		}

		/*
		 *  The two ends (PipeIdnputStream and PipedOutputStream) must be in two different Threads.
		 *
		 *
		 *  sourceStream --> zipperOutStream --> intermediateStream   -->    resultStream ()
		 *     InputStream                        PipedOutputStream         PipedInputStream
		 *                           t1                                            t2
		 */
		final PipedInputStream resultStream = new PipedInputStream();
		final PipedOutputStream intermediateStream = new PipedOutputStream(resultStream);
		final OutputStream zipperOutStream = getCompressionWrapper(intermediateStream, type);

		Runnable copyTask = new Runnable() {

			@Override
			public void run() {
				try {
					int c;
					while((c = sourceStream.read()) >= 0) {
						zipperOutStream.write(c);
					}
					zipperOutStream.flush();
				} catch (IOException e) {
					IOUtils.closeQuietly(resultStream);  // close it on error case only
					throw new RuntimeException(e);
				} finally {
					// close source stream and intermediate streams
					IOUtils.closeQuietly(sourceStream);
					IOUtils.closeQuietly(zipperOutStream);
					IOUtils.closeQuietly(intermediateStream);
				}
			}
		};
		getInstance().pool.submit(copyTask);
		return resultStream;
	}

	public static byte[] getCompressedBytes(InputStream sourceStream)throws IOException {
		InputStream compressedStream = getCompressedStream(sourceStream, CompressionType.GZIP);
		//internally it just makes a ByteArrayOutputStream and use toByteArray()
		//still uses double memory.
		return IOUtils.toByteArray(compressedStream);
	}
}
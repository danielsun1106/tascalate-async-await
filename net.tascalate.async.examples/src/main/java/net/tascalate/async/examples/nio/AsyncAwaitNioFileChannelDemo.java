package net.tascalate.async.examples.nio;

import static net.tascalate.async.api.AsyncCall.asyncResult;
import static net.tascalate.async.api.AsyncCall.await;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.util.Collections;
import java.util.concurrent.CompletionStage;

import net.tascalate.async.api.async;

import net.tascalate.nio.channels.AsynchronousFileChannel;

public class AsyncAwaitNioFileChannelDemo {

	public static void main(final String[] argv) throws Exception {
		final AsyncAwaitNioFileChannelDemo demo = new AsyncAwaitNioFileChannelDemo();
		final CompletionStage<String> result = demo.processFile("./.project");
		
		System.out.println("Returned to caller " + LocalTime.now());
		final CompletionStage<?> waiter = result.whenComplete((r, e) -> {
			if ( e != null ) {
				System.out.println("Error " +  LocalTime.now());
				e.printStackTrace(System.out);
			} else {
				System.out.println("Result " +  LocalTime.now());
				System.out.println(r);
			}
		});
		
		// Need to wait because NIO uses daemon threads that do not prevent program exit
		System.out.println("Start waiting for result to prevent program close...");
		waiter.toCompletableFuture().join();
		
	}

	
	
	public @async CompletionStage<String> processFile(final String fileName) throws IOException {
		final Path path = Paths.get(new File(fileName).toURI());
		try (
				final AsynchronousFileChannel file = AsynchronousFileChannel.open(path, Collections.singleton(StandardOpenOption.READ), null);
				final FileLock lock = await(file.lockAll(true))
			) {
			System.out.println("In process, shared lock: " + lock);
			final ByteBuffer buffer = ByteBuffer.allocateDirect((int)file.size());
			
			await( file.read(buffer, 0L) );
			System.out.println("In process, bytes read: " + buffer);
			buffer.rewind();
   
			final String result = processBytes(buffer);
			 
			return asyncResult(result);
			
		} catch (final IOException ex) {
			ex.printStackTrace(System.out);
			throw ex;
		}
	}
	
	private String processBytes(final ByteBuffer buffer) throws IOException {
		final StringBuilder result = new StringBuilder();
		final char[] chars = new char[4096];
		try (final InputStreamReader in = new InputStreamReader(new ByteBufferInputStream(buffer))) {
			int count = 0;
			while( (count = in.read(chars)) > 0) {
				result.append(chars, 0, count);
			};
			return result.toString();
		}
	}
}

package com.rafkind.reft.stream;

import java.io.IOException;
import java.io.InputStream;

public class StreamingInputStream extends InputStream {

	private StreamReader reader;
	private long mark;
	private Mark current;
	private int max;
	private int position;

	public StreamingInputStream( StreamReader reader ) throws IOException {
		this.reader = reader;
		this.mark = -1;
		this.position = 0;
		this.current = reader.getMark( mark );
		this.mark = current.getId();
		this.max = current.getLength();
	}

	public int read() throws IOException {
		byte[] b = new byte[1];
		read( b );
		return b[ 0 ];
	}

	private void readMark() throws IOException {
		this.current = reader.getMark( mark );
		this.mark = current.getId();
		this.position = 0;
		this.max = current.getLength();
		// System.out.println( "Current mark " + this.mark );
	}

	public int read( byte[] copy ) throws IOException {

		if ( position == max ){
			readMark();
		}

		int size = copy.length;
		int length = max - position;
		int which = length > size ? size : length;
		System.arraycopy( current.getData(), position, copy, 0, which );
		position += which;

		return which;
	}
}

package com.rafkind.reft.stream;

import com.rafkind.reft.Lambda0;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class StreamReader implements Runnable {

	// public static final int BUFFER_SIZE = 1 << 15;
	// public static final int MARK_LENGTH = 1024;
	public static final int MARK_LENGTH = 4096;
	private final int MAX_MARKS = 350;

	private boolean done = false;
	// private List files;
	private boolean dead = true;
	// private Iterator current;
	private FileInputStream reading;

	private int bitRate = 96;

	private List marks;
	private long lastMark;

	private Lambda0 nextFileProc;

	public StreamReader( final Lambda0 proc ){
		this.marks = new ArrayList();
		this.nextFileProc = proc;
	}

	public synchronized void setFile( File f ) throws IOException {
		if ( f == null ){
			throw new IOException( "No file given" );
		}
		this.reading = new FileInputStream( f );
		setBitRate( readBitRate( f ) );
	}

	private synchronized FileInputStream getStream(){
		return this.reading;	
	}

	public void reset() throws IOException {
		this.lastMark = 0;
		this.marks = new ArrayList();

		setFile( nextFile() );
				
		/* fill up cache */
		readMarks( MAX_MARKS );
	}

	private void readMarks( int marks ) throws IOException {
		for ( int i = 0; i < marks; i++ ){
			readMark();
		}
	}

	private synchronized boolean isDone(){
		return this.done;
	}

	public synchronized void setDone( boolean b ){
		this.done = b;
		synchronized( marks ){
			marks.notifyAll();
		}
	}

	public boolean isRunning(){
		return ! dead;
	}

	public void run(){
		dead = false;

		try{
			while ( ! isDone() ){
				readMark();
				rest( (int)(MARK_LENGTH * 1000.0 / getByteRate()) );
			}

			getStream().close();
		} catch ( IOException ie ){
			ie.printStackTrace();
		}

		dead = true;
	}

	/* bit rate of the mp3 */
	private double getByteRate(){
		// return (double) getBitRate() * 1024.0 / 8.0;
		return (double) getBitRate() / 8.0;
	}

	private int readBitRate( File mp3 ) throws IOException {
		FileInputStream stream = new FileInputStream( mp3 );

		try{
			Bitstream b = new Bitstream( stream );
			return b.readFrame().bitrate();
		} catch ( Exception e ){
			throw new IOException( "Could not read bitrate" );
		} finally {
			stream.close();
		}
	}

	private void setBitRate( int s ){
		bitRate = s;
		System.out.println( "Bit rate = " + s );
	}

	private int getBitRate(){
		return bitRate;
	}

	private Mark findMark( long mark ) throws IOException {
		Mark m = null;
		if ( isDone() ){
			throw new IOException( "Server finished serving data" );
		}
		synchronized( marks ){
			for ( Iterator iterator = marks.iterator(); iterator.hasNext(); ){
				Mark next = (Mark) iterator.next();
				if ( next.getId() > mark ){
					return m = next;
				}
			}
		}
		return m;
	}

	public Mark getMark( long mark ) throws IOException {
		Mark m = null;
		while ( m == null ){
			synchronized( marks ){
				m = findMark( mark );
				if ( m == null ){
					try{
						marks.wait();
					} catch ( Exception e ){
					}
				}
			}
		}
		return m;
	}

	private void readMark() throws IOException {
		byte[] b = new byte[ MARK_LENGTH ];
		FileInputStream stream = getStream();
		int length = stream.read( b );
		if ( length == -1 ){
			stream.close();

			setFile( nextFile() );
			
			readMark();
		} else {
			synchronized( marks ){
				marks.add( new Mark( b, length, lastMark ) );
				lastMark += 1;
				while ( marks.size() > MAX_MARKS ){
					Mark r = (Mark) marks.remove( 0 );
					// System.out.println( "Throwing out mark " + r.getId() );
				}
				
				marks.notifyAll();
			}
		}
	}

	private File nextFile(){
		return (File) nextFileProc.invoke_();
	}

	private void rest( int n ){
		try{
			Thread.sleep( n );
		} catch ( Exception i ){
		}
	}
}

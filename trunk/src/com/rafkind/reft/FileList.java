package com.rafkind.reft;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;

public class FileList implements ListModel {

	private List listeners;
	private List files;
	private int nextSong;
	private int currentSong;

	public FileList(){
		this.listeners = new ArrayList();
		this.files = new ArrayList();
		currentSong = 0;
		nextSong = 1;
	}

	public void addListDataListener( ListDataListener l ){
		this.listeners.add( l );
	}

	public Object getElementAt( int index ){
		return this.files.get( index );
	}

	public int getSize(){
		return this.files.size();
	}

	public List getList(){
		return new ArrayList( files );
	}

	public void add( File f ){
		this.files.add( f );
		ListDataEvent event = new ListDataEvent( this, ListDataEvent.INTERVAL_ADDED, this.files.size() - 1, this.files.size() - 1 );
		for ( Iterator it = this.listeners.iterator(); it.hasNext(); ){
			ListDataListener l = (ListDataListener) it.next();
			l.intervalAdded( event );
		}
	}

	public void removeAll( Object[] objects ){
		for ( int i = 0; i < objects.length; i++ ){
			this.files.remove( objects[ i ] );
		}

		ListDataEvent event = new ListDataEvent( this, ListDataEvent.CONTENTS_CHANGED, 0, 999999 );
		for ( Iterator it = this.listeners.iterator(); it.hasNext(); ){
			ListDataListener l = (ListDataListener) it.next();
			l.intervalRemoved( event );
		}
	}

	public void removeListDataListener( ListDataListener l ){
		this.listeners.remove( l );
	}

	public synchronized void setCurrentSong( int index ){
		nextSong = index + 1;
	}

	public synchronized int getCurrentSong(){
		return nextSong;
	}
	
	private synchronized int getNextSong(){
		currentSong = nextSong;
		nextSong += 1;
		if ( currentSong >= getSize() ){
			currentSong = 0;
			nextSong = 1;
		}
		return currentSong;
	}

	public Lambda0 getNextFileLambda(){
		return new Lambda0(){
			public Object invoke(){
				int r = getNextSong();
				return (File) getElementAt( r );
			}
		};
	}
}

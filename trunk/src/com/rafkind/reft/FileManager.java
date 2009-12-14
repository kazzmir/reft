package com.rafkind.reft;

import java.util.HashMap;

import java.util.List;
import java.util.ArrayList;

import java.io.File;

/* handle virtual files
 */
public class FileManager{

	private HashMap directories;

	public FileManager(){
		directories = new HashMap();
		directories.put( "/root", new HashMap() );
	}

	public synchronized List getFileList( String path ){
		path = sanitizeDirectory( path );
		// System.out.println( "Get file list '" + path + "'" );
		HashMap dir = (HashMap) directories.get( path );
		if ( dir != null ){
			List files = new ArrayList();
			files.addAll( dir.values() );
			return files;
		}
		return null;
	}

	public synchronized void addDirectory( String directory ){
		directories.put( directory, new HashMap() );
	}

	public synchronized void removeDirectory( String directory ){
		directories.remove( directory );
	}

	public synchronized void removeFile( String directory, File file ){
		HashMap dir = (HashMap) directories.get( directory );
		if ( dir != null ){
			dir.remove( file.getName() );
		}
	}

	public synchronized void addFile( String directory, File file ){
		HashMap dir = (HashMap) directories.get( directory );
		if ( dir != null ){
			dir.put( file.getName(), file );
		}
	}

	public File getFile( String directory, String path ){
		if ( path.startsWith( "/" ) ){
			return getFile( path );
		} else {
			return getFile( directory + "/" + path );
		}
	}

	private String sanitizeDirectory( String path ){
		String[] parts = path.split( "/" );
		StringBuffer buffer = new StringBuffer();
		int length = parts.length > 2 ? parts.length - 1 : 2;
		for ( int i = 0; i < length; i++ ){
			if ( ! parts[ i ].equals( "" ) ){
				buffer.append( "/" + parts[ i ] );
			}
		}
		return buffer.toString();
	}

	public synchronized File getFile( String path ){

		String directory = sanitizeDirectory( path );
		String file = path.substring( path.lastIndexOf( "/" ) + 1 );

		HashMap dir = (HashMap) directories.get( directory );
		// System.out.println( "Manager: directory[ " + directory + " ] file[ " + file + " ] = " + dir );
		if ( dir != null ){
			return (File) dir.get( file );
		}
		return null;
	}
}

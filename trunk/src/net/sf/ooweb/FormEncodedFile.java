/*
 * OOWeb
 *    
 * Copyright(c)2005, OOWeb developers (see the accompanying "AUTHORS" file)
 *
 * This software is licensed under the 
 * GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1
 *    
 * For more information on distributing and using this program, please
 * see the accompanying "COPYING" file.
 */
package net.sf.ooweb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Represents a file received via a multipart/form-data POST
 * @author Robin Rawson-Tetley
 */
public class FormEncodedFile {

	private String filename = "";
	private String mimeType = "";
	private String name = "";
	private byte[] data = null;
	
	public FormEncodedFile(String name, String filename, String mimeType, byte[] data) {
		this.filename = filename;
		this.mimeType = mimeType;
		this.name = name;
		this.data = data;
	}
	
	public String getName() {
		return name;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getMimeType() {
		return mimeType;
	}

	public byte[] getData() {
		return data;
	}
	
	/** Saves the file to disk */
	public void saveToFile(File f) throws IOException {
		FileOutputStream o = new FileOutputStream(f);
		o.write(data);
		o.flush();
		o.close();
	}
	
	public String toString() {
		return "[FormEncodedFile, name=" + name + ", filename=" + filename +
			", mimeType=" + mimeType + ", data=" + data.length + "bytes]";
	}
	
}

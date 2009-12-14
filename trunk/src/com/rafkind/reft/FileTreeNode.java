package com.rafkind.reft;

import java.io.File;

import java.util.Enumeration;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;

public class FileTreeNode extends DefaultMutableTreeNode {

	private boolean directory = false;
	private String name;

	public FileTreeNode( File f ){
		this( f, f.isDirectory() );
	}

	public FileTreeNode( File f, boolean directory ){
		super( f );
		this.directory = directory;
		name = f.getName();
	}
	
	public FileTreeNode( String s, boolean directory ){
		super( s );
		this.directory = directory;
		name = s;
	}

	public long lastModified(){
		if ( getUserObject() instanceof String ){
			return 0;
		}

		return ((File) this.getUserObject()).lastModified();
	}

	public long length(){
		if ( getUserObject() instanceof String ){
			return 0;
		}

		return ((File) this.getUserObject()).length();
	}

	public List getFiles(){
		List files = new ArrayList();
		Enumeration children = this.children();
		while ( children.hasMoreElements() ){
			files.add( children.nextElement() );
		}

		return files;
	}

	public List getDirectories(){
		List dirs = new ArrayList();
		Enumeration children = this.children();
		while ( children.hasMoreElements() ){
			FileTreeNode f = (FileTreeNode) children.nextElement();
			if ( f.isDirectory() ){
				dirs.add( f );
			}
		}

		return dirs;
	}

	public FileTreeNode findChild( String name ){
		if ( ".".equals( name ) ){
			return this;
		}
		List files = getFiles();
		for ( Iterator iterator = files.iterator(); iterator.hasNext(); ){
			FileTreeNode node = (FileTreeNode) iterator.next();
			if ( node.getName().equals( name ) ){
				return node;
			}
		}

		return null;
	}

	/* this will only be called when the user edits a cell */
	public void setUserObject( Object o ){
		name = (String) o;
	}

	public String getLineage(){
		if ( getParent() == null ){
			return "";
		}
		return ((FileTreeNode) getParent()).getLineage() + "/" + getName();
	}

	public File getFile(){
		return (File) this.getUserObject();
	}

	public String getName(){

		return name;
		/*
		if ( this.getUserObject() instanceof String ){
			return this.getUserObject().toString();
		}

		return ((File) this.getUserObject()).getName();
		*/
	}

	public String toString(){
		return name;
	}

	public boolean isDirectory(){
		return this.directory;
	}
}

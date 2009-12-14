package com.rafkind.reft;

import java.util.Vector;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.io.File;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.DefaultMutableTreeNode;

public class FileTree extends DefaultTreeModel implements TreeModel {

	public FileTree(){
		super( new FileTreeNode( "/", true ) );

		/*
		this.getTreeRoot().add( new DefaultMutableTreeNode( "F" ) );
		this.getTreeRoot().add( new DefaultMutableTreeNode( "G" ) );
		*/
	}

	private FileTreeNode getTreeRoot(){
		return (FileTreeNode) super.getRoot();
	}

	public void remove( TreePath[] paths ){
	}

	public void insert( TreeNode node, int[] children ){
		nodesWereInserted( node, children );
	}

	public List getFileList( String path ) throws Exception {
		// System.out.println( "List path " + path );
		synchronized( getRoot() ){
			String[] paths = path.substring( 1 ).split( "/" );
			FileTreeNode node = getTreeRoot();
			for ( int i = 0; i < paths.length; i++ ){
				// System.out.println( "Find child " + paths[ i ] );
				node = node.findChild( paths[ i ] );	
				if ( node == null ){
					throw new Exception( "No such directory" + paths[ i ] );
				}
			}
			return node.getFiles();	
		}
	}

	public File getFile( String directory, String path ) throws Exception {
		// System.out.println( "Send file " + directory + "/" + path );
		if ( ! path.startsWith( "/" ) ){
			path = directory + "/" + path;
		}
		synchronized( getRoot() ){
			String[] paths = path.substring( 1 ).split( "/" );
			FileTreeNode node = getTreeRoot();
			for ( int i = 0; i < paths.length; i++ ){
				// System.out.println( "Find child " + paths[ i ] );
				node = node.findChild( paths[ i ] );	
				if ( node == null ){
					throw new Exception( "No such directory " + paths[ i ] );
				}
			}
			if ( node.isDirectory() ){
				return null;
			}
			return (File) node.getUserObject();
		}
	}

	public void remove( MutableTreeNode node ){
		if ( node != getRoot() ){
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
			int index = parent.getIndex( node );
			parent.remove( node );
			this.nodesWereRemoved( parent, new int[]{ index }, new Object[]{ node } );
		}
	}

	public FileTreeNode findNode( String[] paths ){
		FileTreeNode root = getTreeRoot();
		/*
		for ( int i = 0; i < paths.length; i++ ){
			System.out.print( "<" + paths[ i ] + "> " );
		}
		System.out.println( "" );
		*/
		// System.out.println( "Paths: " + paths.length );
		for ( int i = 0; i < paths.length; i++ ){
			System.out.println( "Root: " + root.getName() + " Find node for " + paths[ i ] );
			FileTreeNode child = root.findChild( paths[ i ] );
			if ( child != null && child.isDirectory() ){
				root = child;
			} else {
				return null;
			}
		}
		return root;
	}

	public void removeAll( TreePath[] paths ){
		for ( int i = 0; i < paths.length; i++ ){
			remove( (MutableTreeNode) paths[ i ].getLastPathComponent() );
		}
	}

	/*
	public void add( Object o ){
		
		FileTreeNode newNode = new FileTreeNode( o );
		getTreeRoot().add( newNode );

		TreePath path = new TreePath( getRoot() );
		path = path.pathByAddingChild( newNode );

		nodeStructureChanged( getTreeRoot() );

		/ *
		for ( Iterator iterator = listeners.iterator(); iterator.hasNext(); ){
			TreeModelListener t = (TreeModelListener) iterator.next();
			t.treeNodesInserted( new TreeModelEvent( getRoot(), path ) );
			
		}* /
	}
	*/
}

package com.rafkind.reft;

import java.io.IOException;
import java.io.File;
import java.io.FileFilter;

import java.util.Vector;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.awt.Component;
import java.awt.Color;

import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.Dimension;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import javax.swing.BorderFactory;
import javax.swing.filechooser.FileSystemView;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ListCellRenderer;
import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.JSplitPane;
import javax.swing.JList;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JSeparator;
import javax.swing.JScrollPane;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.Box;

public class FileCollectorDialog extends JDialog {

	public FileCollectorDialog( final Lambda1 sendFilesLambda ) throws Exception {
		super( (JFrame) null, "Select files" );
		final JDialog dialog = this;
		// JPanel all = new JPanel();
		JSplitPane split = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
		split.setResizeWeight( 0.9 );
		this.getContentPane().add( split );

		split.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

		// Box vertical = Box.createVerticalBox();
		// all.add( split );

		Box windows = Box.createHorizontalBox();
		// vertical.add( windows );
		split.setTopComponent( windows );

		JComponent fileView = Box.createVerticalBox();
		windows.add( fileView );
		final SString currentDirectoryPath = new SString();
		currentDirectoryPath.setPath( getFullPath( "." ) );
		final FileSystemView systemView = FileSystemView.getFileSystemView();
		
		// final JTextField currentDirectory = new JTextField();
		final DirectoryModel directoryModel = new DirectoryModel();
		final JComboBox rootDirectory = new JComboBox( directoryModel );
		rootDirectory.setMaximumSize( new Dimension( 320, 30 ) );
		rootDirectory.setToolTipText( currentDirectoryPath.getPath() );
		rootDirectory.setRenderer( new FileViewRenderer() );

		directoryModel.updateRoot( currentDirectoryPath.getPath() );

		final FileListModel fileModel = new FileListModel( new FileFilter(){
			public boolean accept( File path ){
				return ! path.isDirectory();
			}
		});

		final FileListModel directoryListModel = new FileListModel( new FileFilter(){
			public boolean accept( File path ){
				return path.isDirectory();
			}
		});

		final Lambda1 setDirectory = new Lambda1(){
			public Object invoke( final Object o ){

				final File file = (File) o;
				currentDirectoryPath.setPath( getFullPath( file.getAbsolutePath() ) );
				directoryModel.updateRoot( currentDirectoryPath.getPath() );
				rootDirectory.setToolTipText( currentDirectoryPath.getPath() );
				fileModel.updateFiles( file );
				directoryListModel.updateFiles( file );
				return null;
			}
		};

		setDirectory.invoke( new File( "." ) );
		fileView.add( rootDirectory );

		rootDirectory.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent action ){
				File file = new File( (String) rootDirectory.getSelectedItem() );
				try{
					setDirectory.invoke( file );
				} catch ( Exception ex ){
				}
			}
		});

		fileView.add( Box.createVerticalStrut( 5 ) );
		JButton addFile = new JButton( "Add File" );
		final JList fileList = new JList( fileModel );
		fileView.add( addFile );
		fileView.add( new JScrollPane( fileList ) );

		fileView.add( Box.createVerticalStrut( 5 ) );

		Box dirButtons = Box.createHorizontalBox();
		JButton changeDir = new JButton( "Change directory" );
		dirButtons.add( changeDir );

		dirButtons.add( Box.createHorizontalStrut( 5 ) );

		JButton addDir = new JButton( "Add Directory" );
		dirButtons.add( addDir );
		fileView.add( dirButtons );
		final JList directoryList = new JList( directoryListModel );
		fileView.add( new JScrollPane( directoryList ) );
		
		changeDir.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent event ){
				int index = directoryList.getSelectedIndex();
				if ( index != -1 ){
					File file = directoryListModel.getFileAt( index );
					setDirectory.invoke_( file );
				}
			}
		});

		directoryList.addMouseListener( new MouseAdapter(){
			public void mouseClicked( MouseEvent e ){
				if ( e.getClickCount() == 2 ){
					int index = directoryList.locationToIndex( e.getPoint() );
					if ( index != -1 ){
					File file = directoryListModel.getFileAt( index );
					setDirectory.invoke_( file );
					}
				}
			}
		});

		// fileModel.updateFiles( new File( currentDirectoryPath.getPath() ) );

		/*
		directoryList.addMouseListener( new MouseAdapter(){
			public void mouseClicked( MouseEvent e ){
				if ( e.getClickCount() == 2 ){
					int index = directoryList.locationToIndex( e.getPoint() );
					File file = directoryListModel.getFileAt( index );
					// fileModel.updateFiles( file );
					// directoryListModel.updateFiles( file );
					try{
						setDirectory.invoke( file );
					} catch ( Exception ex ){
					}
				}
			}
		});
		*/

		final Lambda0 getSelectedFiles = new Lambda0(){
			public Object invoke(){
				int[] index = fileList.getSelectedIndices();
				List files = new ArrayList();
				for ( int i = 0; i < index.length; i++ ){
					files.add( fileModel.getFileAt( index[ i ] ) );
				}
				return files;
			}
		};

		final Lambda0 getSelectedDirectories = new Lambda0(){
			public Object invoke(){
				int[] index = directoryList.getSelectedIndices();
				List files = new ArrayList();
				for ( int i = 0; i < index.length; i++ ){
					files.add( directoryListModel.getFileAt( index[ i ] ) );
				}
				return files;
			}
		};

		windows.add( Box.createHorizontalStrut( 5 ) );

		Box moreButtons = Box.createVerticalBox();
		windows.add( moreButtons );

		// JButton add = new JButton( "Add" );
		// moreButtons.add( add );
		final JButton remove = new JButton( "Remove" );
		moreButtons.add( Box.createVerticalStrut( 5 ) );
		moreButtons.add( remove );
		remove.setEnabled( false );
		
		windows.add( Box.createHorizontalStrut( 5 ) );

		final SendListModel toSendModel = new SendListModel(){
			List listeners = new ArrayList();
			List files = new ArrayList();

			public void addListDataListener( ListDataListener l ){
				listeners.add( l );
			}

			public Object getElementAt( int index ){
				File file = (File) files.get( index );
				if ( file.isDirectory() ){
					return file.getName() + "/";
				} else {
					return file.getName();
				}
			}

			public void removeFile( File file ){
				files.remove( file );
				updateList();
			}

			public File getFileAt( int i ){
				return (File) files.get( i );
			}

			public boolean isEmpty(){
				return files.isEmpty();
			}

			public void addFile( File file ){
				files.add( file );
				updateList();
			}

			private void updateList(){
				try{
					Lambda1.foreach( listeners, new Lambda1(){
						public Object invoke( Object o ){
							ListDataListener l = (ListDataListener) o;
							l.contentsChanged( new ListDataEvent( this, ListDataEvent.CONTENTS_CHANGED, 0, files.size() ) );
							return null;
						}
					});
				} catch ( Exception ex ){
				}
			}

			public int getSize(){
				return files.size();
			}

			public void removeListDataListener( ListDataListener l ){
				listeners.remove( l );
			}
		};

		addFile.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent action ){
				try{
					List files = (List) getSelectedFiles.invoke();
					Lambda1.foreach( files, new Lambda1(){
						public Object invoke( Object o ){
							File f = (File) o;
							toSendModel.addFile( f );
							return null;
						}
					});
					remove.setEnabled( true );
				} catch ( Exception ex ){
				}
			}
		});

		addDir.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent action ){
				try{
					List files = (List) getSelectedDirectories.invoke();
					Lambda1.foreach( files, new Lambda1(){
						public Object invoke( Object o ){
							File f = (File) o;
							toSendModel.addFile( f );
							return null;
						}
					});
				} catch ( Exception ex ){
				}
				remove.setEnabled( true );
			}
		});


		final JList toSendList = new JList( toSendModel );
		Box boxxy = Box.createVerticalBox();
		boxxy.add( new JLabel( "Files to send" ) );
		boxxy.add( Box.createVerticalStrut( 5 ) );
		boxxy.add( new JScrollPane( toSendList ) );
		windows.add( boxxy );

		remove.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent event ){
				int[] indicies = toSendList.getSelectedIndices();
				List files = new ArrayList();
				for ( int i = 0; i < indicies.length; i++ ){
					files.add( toSendModel.getFileAt( indicies[ i ] ) );
				}
				try{
					Lambda1.foreach( files, new Lambda1(){
						public Object invoke( Object o ){
							File f = (File) o;
							toSendModel.removeFile( f );
							return null;
						}
					});
				} catch ( Exception ex ){
				}
				if ( toSendModel.isEmpty() ){
					remove.setEnabled( false );
				}
			}
		});

		toSendList.addListSelectionListener( new ListSelectionListener(){
			public void valueChanged( ListSelectionEvent event ){
				remove.setEnabled( true );
			}
		});

		/*
		vertical.add( Box.createVerticalStrut( 5 ) );
		vertical.add( new JSeparator( SwingConstants.HORIZONTAL ) );
		vertical.add( Box.createVerticalStrut( 5 ) );
		*/

		Box buttons = Box.createHorizontalBox();
		split.setBottomComponent( buttons );

		final Lambda0 close = new Lambda0(){
			public Object invoke(){
				dialog.dispose();
				return null;
			}
		};

		JButton send = new JButton( "Send files" );
		buttons.add( send );
		send.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent action ){
				try{

					if ( ! toSendModel.isEmpty() ){
						File[] files = new File[ toSendModel.getSize() ];
						for ( int i = 0; i < files.length; i++ ){
							files[ i ] = toSendModel.getFileAt( i );
						}
						System.out.println( "Sending " + files.length + " files" );
						sendFilesLambda.invoke( files );
					}

					close.invoke();
				} catch ( Exception ex ){
				}
			}
		});

		buttons.add( Box.createHorizontalStrut( 10 ) );

		JButton cancel = new JButton( "Cancel" );
		buttons.add( cancel );
		cancel.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent action ){
				try{
					close.invoke();
				} catch ( Exception ex ){
				}
			}
		});
	}

	private String getFullPath( String path ){
		try{
			return new File( path ).getCanonicalPath();
		} catch ( IOException ie ){
			return new File( path ).getAbsolutePath();
		}
	}

	private class DirectoryModel implements ComboBoxModel {
		List stuff = new ArrayList();
		Object item = null;
		List listeners = new ArrayList();
		public Object getSelectedItem(){
			return item;
		}

		public void setSelectedItem( Object anItem ){
			item = anItem;
		}

		public void updateRoot( String path ){
			stuff.clear();

			File parent = new File( path );
			while ( parent != null ){
				stuff.add( parent.getAbsolutePath() );
				parent = parent.getParentFile();
			}

			File[] roots = FileSystemView.getFileSystemView().getRoots();
			for ( int i = 0; i < roots.length; i++ ){
				stuff.add( roots[ i ].getAbsolutePath() );
			}
			File[] windowsRoots = findWindowsRoots();
			for ( int i = 0; i < windowsRoots.length; i++ ){
					  stuff.add( windowsRoots[ i ].getAbsolutePath() );
			}

			setSelectedItem( stuff.get( 0 ) );

			try{
				Lambda1.foreach( listeners, new Lambda1(){
					public Object invoke( Object arg ){
						ListDataListener l = (ListDataListener) arg;
						l.contentsChanged( new ListDataEvent( this, ListDataEvent.CONTENTS_CHANGED, 0, stuff.size() ) );
						return null;
					}
				});
			} catch ( Exception ex ){
			}
		}

		private File[] findWindowsRoots(){
			Vector rootsVector = new Vector();
			// Create the A: drive whether it is mounted or not
			/*
			FileSystemRoot floppy = new FileSystemRoot(Resources.getString(FLOPPY_DRIVE) + "\\");
			rootsVector.addElement(floppy);
			*/
                                                                                    
			// Run through all possible mount points and check
			// for their existance.
			for ( char c = 'C'; c <= 'Z'; c++ ){
					  char device[] = {c, ':', '\\'};
					  String deviceName = new String( device );
					  File deviceFile = new FileSystemRoot( deviceName );
					  if ( deviceFile.exists() ){
								 rootsVector.addElement( deviceFile );
					  }
			}
			File[] roots = new File[ rootsVector.size() ];
			rootsVector.copyInto( roots );
			return roots;
		}

		private class FileSystemRoot extends File {
			public FileSystemRoot( File f ){
				super( f, "" );
			}

			public FileSystemRoot( String s ){
				super( s );
			}

			public boolean isDirectory() {
				return true;
			}
		}

		public void addListDataListener( ListDataListener l ){
			listeners.add( l );
		}

		public Object getElementAt( int index ){
			return stuff.get( index );
		}

		public int getSize(){
			return stuff.size();
		}

		public void removeListDataListener( ListDataListener l ){
			listeners.remove( l );
		}
	}

	class FileViewRenderer implements ListCellRenderer {
		private Color backgroundSelected;
		public FileViewRenderer() {
			backgroundSelected = new Color( 240, 124, 96 );
		}

		public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ){
			// JLabel render = new JLabel( value.toString().replaceAll( "/.*?/]", "/../" ), SwingConstants.LEADING );
			JLabel render = new JLabel( value.toString(), SwingConstants.LEFT );
			render.setOpaque( true );
			render.setBackground( isSelected ? backgroundSelected : Color.white );
			render.setForeground( isSelected ? Color.black : Color.black );

			render.setToolTipText( value.toString() );
			return render;
		}
	}

	private class FileListModel implements ListModel {

		private	List files = new ArrayList();
		private	List listeners = new ArrayList();
		private FileFilter filter;

		public FileListModel( FileFilter filter ){
			this.filter = filter;
		}

		public void updateFiles( File directory ){
			File[] find = directory.listFiles( filter );
			Arrays.sort( find, new Comparator(){
				public int compare( Object o1, Object o2 ){
					File f1 = (File) o1;
					File f2 = (File) o2;
					return f1.getName().compareTo( f2.getName() );
				}

				public boolean equals( Object obj ){
					return obj == this;
				}
			});
			files.clear();
			// files.add( new File( ".." ) );
			final int max = 9999999; // whats the max index??
			for ( int i = 0; i < find.length; i++ ){
				files.add( find[ i ] );
			}

			try{
				Lambda1.foreach( listeners, new Lambda1(){
					public Object invoke( Object o ){
						ListDataListener l = (ListDataListener) o;
						l.contentsChanged( new ListDataEvent( this, ListDataEvent.CONTENTS_CHANGED, 0, max ) );
						return null;
					}
				});
			} catch ( Exception ex ){
			}
		}

		public void addListDataListener( ListDataListener l ){
			listeners.add( l );
		}

		public File getFileAt( int index ){
			return (File) files.get( index );
		}

		public Object getElementAt( int index ){
			File file = (File) files.get( index );
			if ( file.isDirectory() ){
				return file.getName() + "/";
			} else {
				return file.getName();
			}
		}

		public int getSize(){
			return files.size();
		}

		public void removeListDataListener( ListDataListener l ){
			listeners.remove( l );
		}
	}

	private interface SendListModel extends ListModel {
		public void addFile( File file );
		public void removeFile( File file );
		public File getFileAt( int i );
		public boolean isEmpty();
	}

	private class SString{
		private String string;

		public SString(){
		}

		public SString( String string ){
			this.string = string;
		}

		public String getPath(){
			return string;
		}

		public void setPath( String string ){
			this.string = string;
		}
	}
}

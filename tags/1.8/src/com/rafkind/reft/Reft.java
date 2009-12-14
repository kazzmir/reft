package com.rafkind.reft;

import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import javax.swing.plaf.metal.DefaultMetalTheme;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.awt.Graphics;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.net.Socket;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerListener;
import java.awt.event.ContainerEvent;
import java.awt.Color;

import java.awt.Frame;
import java.awt.Container;
import java.awt.BorderLayout;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import org.swixml.SwingEngine;

import javax.swing.UIManager;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.filechooser.FileFilter;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
// import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.JSplitPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.SwingUtilities;

// import net.sf.ooweb.annotations.Controller;
import net.sf.ooweb.Configuration;
import net.sf.ooweb.HTTP;

public class Reft extends JFrame {

	private JTabbedPane uploads;
	private JTabbedPane downloads;
	private int listenPort;

	private static final int DIRECTORY = 1;
	private static final int FILE = 0;
	public static final int DEFAULT_REFT_PORT = 4949;
		
	private static final int YES = 1;
	private static final int NO = 0;

	private static final int FILENAME_SIZE = 1024;

	private Object lock;

	public Reft( LoadingWindow loader, Object lock ) throws IOException {
		super( "Reft" );
		this.setSize( 700, 450 );
		this.lock = lock;
		listenPort = DEFAULT_REFT_PORT;

		loader.update( 1 );
			
		JTabbedPane mainPain = new JTabbedPane();
		this.getContentPane().add( mainPain );
		
		loader.update( 4 );

		SwingEngine engine = new SwingEngine( "reft.xml" );
		
		loader.update( 8 );

		JPanel front = (JPanel) engine.getRootComponent();

		mainPain.add( front, "Reft" );

		JMenuBar menuBar = new JMenuBar();
		JMenu programMenu = new JMenu( "Program" );
		JMenu portMenu = new JMenu( "Settings" );
		JMenu helpMenu = new JMenu( "Help" );
		menuBar.add( programMenu );
		menuBar.add( portMenu );
		menuBar.add( helpMenu );
		this.setJMenuBar( menuBar );
		
		loader.update( 13 );

		JMenuItem help = new JMenuItem( "Help" );
		helpMenu.add( help );
		help.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent action ){
				JDialog help = new HelpDialog();
				help.setLocation( Reft.this.getLocationOnScreen() );
				help.setVisible( true );
			}
		});
		
		loader.update( 15 );

		uploads = (JTabbedPane) engine.find( "uploads" );
		downloads = (JTabbedPane) engine.find( "downloads" );
		
		loader.update( 17 );

		Lambda2 sendFileLambda = new Lambda2(){
			public Object invoke( Object arg1, Object arg2 ){
				sendFile( (Buddy) arg1, (File[]) arg2 );
				return null;
			}
		};

		JPanel buddyList = (JPanel) engine.find( "buddylist" );
		buddyList.add( BuddyList.createBuddyList( sendFileLambda ) );
		
		loader.update( 20 );

		Lambda1 addClient = new Lambda1(){
			public Object invoke( Object arg ){
				Socket client = (Socket) arg;

				addClient( client );

				return null;
			}
		};
		
		loader.update( 30 );

		ServerThread serverThread;
		try{
			serverThread = new ServerThread( this.getListenPort(), addClient );
		} catch ( IOException ie ){
			System.out.println( "Could not bind to " + getListenPort() );
			ie.printStackTrace();
			serverThread = new ServerThread( 0, addClient );
		}
		
		loader.update( 40 );

		final ServerThread server = serverThread;

		JMenuItem setPort = new JMenuItem( "Set port" );
		setPort.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent action ){
				final JDialog dialog = new JDialog( Reft.this );
				Box all = Box.createVerticalBox();
				dialog.getContentPane().add( all );

				dialog.setLocation( Reft.this.getLocationOnScreen() );

				Box portBox = Box.createHorizontalBox();
				all.add( portBox );
				portBox.add( new JLabel( "Port" ) );
				portBox.add( Box.createHorizontalStrut( 5 ) );
				final JTextField port = new JTextField( String.valueOf( getListenPort() ) );
				portBox.add( port );
				portBox.add( Box.createHorizontalStrut( 5 ) );

				all.add( Box.createVerticalStrut( 10 ) );

				final Lambda0 close = new Lambda0(){
					public Object invoke(){
						dialog.dispose();
						return null;
					}
				};

				Box buttons = Box.createHorizontalBox();
				all.add( buttons );
				JButton set = new JButton( "Set port" );
				set.addActionListener( new ActionListener(){
					public void actionPerformed( ActionEvent action ){
						try{
							int portNum = Integer.parseInt( port.getText() );
							setListenPort( portNum );
							server.setPort( portNum );
							close.invoke();
						} catch ( Exception e ){
							System.out.println( "Couldnt change port" );
							e.printStackTrace();
						}
					}
				});
				buttons.add( set );
				buttons.add( Box.createHorizontalStrut( 5 ) );
				JButton exit = new JButton( "Close Window" );
				exit.addActionListener( new ActionListener(){
					public void actionPerformed( ActionEvent action ){
						try{
							close.invoke();
						} catch ( Exception ex ){
						}
					}
				});
				buttons.add( exit );
				
				dialog.setSize( 300, 100 );
				dialog.setVisible( true );
			}
		});
		portMenu.add( setPort );
		
		loader.update( 50 );

		final Lambda0 close = new Lambda0(){
			public Object invoke(){
				Frame[] frames = Reft.this.getFrames();

				for ( int i = 0; i < frames.length; i++ ){
					frames[ i ].dispose();
				}

				server.kill();

				/* hard kill */
				// System.exit( 0 );

				// reft.dispose();
				System.exit( 0 );
				return null;
			}
		};

		JMenuItem programClose = new JMenuItem( "Quit" );
		programMenu.add( programClose );
		programClose.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent action ){
				close.invoke_();
			}
		});
		
		loader.update( 55 );
		
		serverThread.start();
		
		loader.update( 60 );

		FileManager ftpManager = new FileManager();
		
		loader.update( 70 );

		mainPain.add( createHTTPPane(), "HTTP" );
		loader.update( 80 );
		// mainPain.add( createFTPPane( ftpManager ), "FTP" );
		mainPain.add( createFTPPane(), "FTP" );

		loader.update( 90 );
		mainPain.add( createStreamingPane(), "Streaming MP3" );
		
		this.addWindowListener( new CloseHook( close ) );
		
		loader.update( 100 );

		// loader.setVisible( false );
	}

	/*
	public void paint( Graphics g ){
		super.paint( g );
		synchronized( lock ){
			lock.notify();
		}
	}

	public void update( Graphics g ){
		super.update( g );
		synchronized( lock ){
			lock.notify();
		}
	}
	*/

	private JPanel createStreamingPane(){
		return new StreamingPane( Reft.this ).getPanel();
	}
	
	private JPanel createFTPPane(){
		return new FtpPane().getPanel();
	}

	private JPanel createHTTPPane(){
		return new HttpPane().getPanel();
	}

	/* add files in a breadth first search fashion */
	public static int addFiles( FileTreeNode parent, File[] files, Lambda0 continue_, Lambda2 set, int num ){
		List dirs = new ArrayList();
		int top = parent.getChildCount();
		int last = 0;
		int count = num;
		for ( int i = 0; i < files.length; i++ ){
			if ( ! ((Boolean) continue_.invoke_()).booleanValue() ){
				return count;
			}
			File f = files[ i ];
			count += 1;
			FileTreeNode node = new FileTreeNode( f );
			parent.add( node );
			if ( f.isDirectory() ){
				dirs.add( new Object[]{ node, f } );
			}
			/* every 50 iterations update the gui */
			if ( count >= 50 ){
				count = 0;
				int[] children = new int[ i - last ];
				for ( int q = last; q < i; q++ ){
					children[ q - last ] = top + q - last;
				}
				top += i - last;
				last = i;
				set.invoke_( parent, children );
				// System.out.println( "Setting top to " + top + " Last = " + last );
			}
		}
		int[] children = new int[ files.length - last ];
		// System.out.println( "Parent = " + parent + " Top: " + top + " Children = " + parent.getChildCount() + " Adding from " + last + " to " + files.length );
		for ( int q = last; q < files.length; q++ ){
			children[ q - last ] = top + q - last;
		}
		set.invoke_( parent, children );

		for ( Iterator iterator = dirs.iterator(); iterator.hasNext(); ){
			Object[] o = (Object[]) iterator.next();
			FileTreeNode node = (FileTreeNode) o[ 0 ];
			File f = (File) o[ 1 ];
			File[] more = f.listFiles();
			Arrays.sort( more );
			count = addFiles( node, more, continue_, set, count );
		}

		return count;
	}

	private List traverseDirectory( File dir ){
		List all = new ArrayList();

		File[] files = dir.listFiles();
		for ( int i = 0; i < files.length; i++ ){
			if ( ! files[ i ].isDirectory() ){
				all.add( files[ i ] );
			} else {
				all.addAll( traverseDirectory( files[ i ] ) );
			}
		}

		return all;
	}

	private Object loadObject( String file, Object default_ ){
		try{
			return (new ObjectInputStream( new FileInputStream( file ) ).readObject());
		} catch ( Exception e ){
			return default_;
		}
	}

	private void saveObject( Object obj, String file ){
		try{
			new ObjectOutputStream( new FileOutputStream( file ) ).writeObject( obj );
		} catch ( Exception e ){
			/* wha-wha-wha-whatever */
		}
	}

	private void addBar( final JTabbedPane space, final JComponent bar, final String title ){
		SwingUtilities.invokeLater( new Runnable(){

			public void run(){
				// space.add( Box.createVerticalStrut( 5 ) );
				space.add( title, bar );
				space.revalidate();
			}
		});
	}

	private void removeBar( JComponent space, JComponent bar ){
		space.remove( bar );
		space.revalidate();
	}

	/*
	private void refresh( JComponent space ){
		space.revalidate();
	}
	*/

	private void addUploadBar( JComponent bar, String title ){
		addBar( uploads, bar, title );
	}

	/*
	private void refreshUploads(){
		refresh( uploads );
	}
	*/

	private void removeUploadBar( JComponent bar ){
		removeBar( uploads, bar );
	}

	private void addDownloadBar( JComponent bar, String title ){
		addBar( downloads, bar, title );
	}

	private void removeDownloadBar( JComponent bar ){
		removeBar( downloads, bar );
	}

	/** Dialog where user can select YES or NO
	 */
	private class OKDialog extends JDialog {

		private int state = YES;

		public OKDialog( JFrame frame, int files, long size, String ip ){
			super( frame, "Reft Transfer", true );
			this.setSize( 300, 200 );

			SwingEngine engine = new SwingEngine( "ok.xml" );

			JPanel panel = (JPanel) engine.getRootComponent();
			this.getContentPane().add( panel );

			JLabel ipLabel = (JLabel) engine.find( "ip" );
			ipLabel.setText( ip + " wants to send you files" );
			JLabel filesLabel = (JLabel) engine.find( "files" );
			filesLabel.setText( "Files: " + files );
			JLabel sizeLabel = (JLabel) engine.find( "size" );
			sizeLabel.setText( "Total size: " + size );

			/*
			JPanel panel = new JPanel();
			this.getContentPane().add( panel );

			Box layout = Box.createVerticalBox();
			panel.add( layout );

			JTextArea area = new JTextArea( ip + " wants to send you " + files + " files, a total of size = " + size + " bytes" );
			area.setLineWrap( true );
			area.setWrapStyleWord( true );
			area.setEditable( false );
			layout.add( area );
			layout.add( Box.createVerticalStrut( 10 ) );
			*/

			final JDialog dialog = this;
			final Lambda0 close = new Lambda0(){
				public Object invoke(){
					dialog.dispose();
					return null;
				}
			};

			Box buttons = Box.createHorizontalBox();
			JButton yes = (JButton) engine.find( "ok" );
			JButton no = (JButton) engine.find( "no" );

			yes.addActionListener( new ActionListener(){
				public void actionPerformed( ActionEvent action ){
					yes();
					try{
						close.invoke();
					} catch ( Exception ex ){
					}
				}
			});

			no.addActionListener( new ActionListener(){
				public void actionPerformed( ActionEvent action ){
					no();
					try{
						close.invoke();
					} catch ( Exception ex ){
					}
				}
			});

			/*
			buttons.add( yes );
			buttons.add( Box.createHorizontalStrut( 5 ) );
			buttons.add( no );
			
			layout.add( buttons );
			*/
		}

		private void no(){
			this.state = NO;
		}

		private void yes(){
			this.state = YES;
		}

		public int getResponse(){
			return this.state;
		}
	}

	/** Receive a bunch of files. Protocol works like this
	 * 1. Sender sends number of files
	 * 2. Sender sends combined size of all files
	 * 3. Receiver sends back YES or NO to continue the transfer or not
	 * 4. For each file loop steps 5-9
	 * 5. Sender sends filename
	 * 6. Sender sends filetype( FILE or DIRECTORY )
	 * 7. If filetype is FILE the size of the file is sent
	 * 8. If filetype is FILE the contents of the file is sent
	 * 9. If filetype is DIRECTORY the directory is created
	 */
	private void addClient( final Socket client ){
		System.out.println( "Adding client" );
		final JFrame frame = this;
		Thread thread = new Thread(){

			private boolean okToReceiveFile( int files, long size ){
				OKDialog ok = new OKDialog( frame, files, size, client.getInetAddress().getHostAddress() );
				ok.setLocation( frame.getLocationOnScreen() );
				ok.setVisible( true );

				return ok.getResponse() == YES;
			}

			private String getDirectory() throws IOException {
				JFileChooser chooser = new JFileChooser( new File( "." ) );
				chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
				int ret = chooser.showOpenDialog( frame );
				if ( ret == JFileChooser.APPROVE_OPTION ){
					return chooser.getSelectedFile().getAbsolutePath();
				}
				throw new IOException( "No directory chosen" );
			}

			private boolean receiveFile( final JComponent add, String filename, long size, final Lambda1 updateLambda, MutableBoolean canceled ) throws IOException {
				InputStream input = new DataInputStream( client.getInputStream() );
				OutputStream output = new BufferedOutputStream( new DataOutputStream( new FileOutputStream( new File( filename ) ) ) );

				Lambda1 addComponent = new Lambda1(){
					public Object invoke( Object arg ){
						final JComponent jarg = (JComponent) arg;
						jarg.setAlignmentX( JComponent.LEFT_ALIGNMENT );
						SwingUtilities.invokeLater( new Runnable(){
							public void run(){
								add.add( jarg );
							}
						});
						// addDownloadBar( jarg );
						return null;
					}
				};
				Transfer transfer = new Transfer( input, output, addComponent, filename, size, client.getInetAddress().getHostAddress(), updateLambda, canceled );
				boolean result = transfer.transfer();

				output.flush();

				return result;
			}
			
			private int receiveNumberOfFiles() throws IOException {
				return new DataInputStream( client.getInputStream() ).readInt();
			}

			private long receiveSize() throws IOException {
				return new DataInputStream( client.getInputStream() ).readLong();
			}
			
			private long receiveFileSize() throws IOException {
				return new DataInputStream( client.getInputStream() ).readLong();
			}
			
			private int receiveFileType() throws IOException {
				return new DataInputStream( client.getInputStream() ).readInt();
			}

			private String receiveFileName() throws IOException {
				byte[] buf = new byte[ FILENAME_SIZE ];
				// System.out.println( "Receiving filename. Ready = " + client.getInputStream().available() );
				DataInputStream stream = new DataInputStream( client.getInputStream() );
				for ( int i = 0; i < FILENAME_SIZE; i++ ){
					buf[ i ] = stream.readByte();
				}
				// System.out.println( "Received bytes: " + bytes );

				return new String( buf ).trim();
			}

			/* send a '1' meaning ok to send
			 */
			private void sendOk() throws IOException {
				DataOutputStream stream = new DataOutputStream( client.getOutputStream() );
				stream.writeByte( YES );
				stream.flush();
			}

			/* send a '0' meaning not ok to send
			 */
			private void sendNotOk() throws IOException {
				DataOutputStream stream = new DataOutputStream( client.getOutputStream() );
				stream.writeByte( NO );
				stream.flush();
			}

			public void run(){
				try{
					// System.out.println( "Receive number of files" );
					final int files = receiveNumberOfFiles();

					final long size = receiveSize();
					// System.out.println( "Received size = " + size );
					if ( okToReceiveFile( files, size ) ){
						String directory = getDirectory();
						sendOk();

						final JComponent description = Box.createVerticalBox();
						description.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
						description.add( new JLabel( "From: " + client.getInetAddress().getHostAddress() ) );
						addDownloadBar( description, client.getInetAddress().getHostAddress() );
						final Color errorColor = new Color( 255, 0, 0 );
						final Lambda1 setTabColor = new Lambda1(){
							public Object invoke( Object o ){
								JTabbedPane pane = Reft.this.downloads;
								final int index = pane.indexOfComponent( description );
								pane.setForegroundAt( index, (Color) o );
								return null;
							}
						};

						description.add( Box.createVerticalStrut( 5 ) );
						final JLabel info = new JLabel();
						description.add( info );
						description.add( Box.createVerticalStrut( 5 ) );

						final SpeedLabel speed = new SpeedLabel();
						final JProgressBar totalProgress = new JProgressBar( 0, 100 );
						final MutableLong mutableLong = new MutableLong();
						final TimeLeft time = new TimeLeft( size );
						final MutableLong updateTime = new MutableLong( System.currentTimeMillis() );

						Lambda1 updateTotalProgress = new Lambda1(){
							public Object invoke( Object o ){
								mutableLong.add( ((Long) o).longValue() );
								if ( System.currentTimeMillis() - updateTime.getValue() > 80 ){
									speed.update( mutableLong.getValue() );
									time.update( mutableLong.getValue(), speed.rawSpeed() );
									int value = (int)( mutableLong.getValue() * 100.0 / size);
									if ( value != totalProgress.getValue() ){
										totalProgress.setValue( value );
										setTabColor.invoke_( new Color( 0, 32 + value * (255-64) / 100, 0 ) );
									}
									updateTime.setValue( System.currentTimeMillis() );
								}
								return null;
							}
						};

						description.add( speed );
						description.add( time );
						description.add( totalProgress );

						JButton details = new JButton( "Show details" );
						details.setToolTipText( "Show more information about the transfer" );
						description.add( Box.createVerticalStrut( 5 ) );
						final Box buttons = Box.createHorizontalBox();
						buttons.setAlignmentX( JComponent.LEFT_ALIGNMENT );
						buttons.add( details );
						buttons.add( Box.createHorizontalStrut( 10 ) );
						
						final MutableBoolean canceled = new MutableBoolean( false );
						final Lambda0 addRemoveButton = new Lambda0(){
							public Object invoke(){
								SwingUtilities.invokeLater( new Runnable(){
									public void run(){
										if ( canceled.isTrue() ){
											info.setText( "Canceled!" );
										} else {
											info.setText( "Done!" );
										}


										JButton remove = new JButton( "Remove transfer" );
										remove.setToolTipText( "Removes this transfer window from the view" );
										buttons.add( Box.createHorizontalStrut( 5 ) );
										buttons.add( remove );
										remove.addActionListener( new ActionListener(){
											public void actionPerformed( ActionEvent action ){
												removeDownloadBar( description );
											}
										});
									}
								});

								return null;
							}
						};

						JButton cancel = new JButton( "Cancel" );
						cancel.setToolTipText( "Cancel this transfer" );
						buttons.add( cancel );
						description.add( buttons );

						cancel.addActionListener( new ActionListener(){
							public void actionPerformed( ActionEvent action ){
								canceled.setTrue();
								try{
									addRemoveButton.invoke();
								} catch ( Exception ex ){
								}
							}
						});

						final Box addMoreBox = Box.createVerticalBox();
						final JComponent addMore = new JScrollPane( addMoreBox );
						addMore.setVisible( false );
						addMore.setAlignmentX( JComponent.LEFT_ALIGNMENT );
						description.add( addMore );

						details.addActionListener( new ActionListener(){
							private boolean set = false;
							public void actionPerformed( ActionEvent action ){
								set = ! set;
								addMore.setVisible( set );
								description.repaint();
							}
						});

						try{
							for ( int i = 0; i < files && canceled.isFalse(); i++ ){

								// System.out.println( "Receiving file " + i );

								String filename = receiveFileName();
								System.out.println( "Receiving Filename = '" + filename + "'" );
								String all = directory + "/" + filename;
								int type = receiveFileType();
								if ( type == DIRECTORY ){
									// System.out.println( "Creating directory " + all );
									new File( all ).mkdirs();
								} else {
									long fileSize = receiveFileSize();
									// System.out.println( "Received filesize = " + fileSize );
									if ( receiveFile( addMoreBox, all, fileSize, updateTotalProgress, canceled ) ){
										System.out.println( "Done receiving file" );
									} else {
										System.out.println( "Error receiving file" );
										SwingUtilities.invokeLater( new Runnable(){
											public void run(){
												setTabColor.invoke_( errorColor );
											}
										});
									}
								}
							}
						} catch ( IOException ie ){
							SwingUtilities.invokeLater( new Runnable(){
								public void run(){
									setTabColor.invoke_( errorColor );
								}
							});
							ie.printStackTrace();
						}

						if ( canceled.isFalse() ){
							try{
								addRemoveButton.invoke();
							} catch ( Exception ex ){
							}
						}
					} else {
						sendNotOk();
					}
					client.close();
				} catch ( IOException ie ){
					
					ie.printStackTrace();
					try{
						client.close();
					} catch ( IOException ie2 ){
						System.out.println( "Could not close client socket" );
						ie2.printStackTrace();
					}
				} finally {
				}
			}
		};

		thread.start();
	}

	/** Send a bunch of files. Protocol should work this way:
	 * 1. Send total number of files( including recursing through directories )
	 * 2. Send total number of bytes of all files
	 * 3. Wait for OK to continue or not ok to cancel
	 * 4. For each file loop steps 5-8
	 * 5. Send filename
	 * 6. Send filetype
	 * 7. If filetype is FILE, send size of file
	 * 8. If filetype is FILE, send contents of file
	 */
	public void sendFile( final Buddy buddy, final File[] files ){
		Thread thread = new Thread(){

			public int countFiles( File[] files ){
				int total = 0;
				for ( int i = 0; i < files.length; i++ ){
					if ( files[ i ].isDirectory() ){
						total += countFiles( files[ i ].listFiles() ) + 1;
					} else {
						total++;
					}
				}
				return total;
			}

			public long countSize( File[] files ){
				long total = 0;
				for ( int i = 0; i < files.length; i++ ){
					if ( files[ i ].isDirectory() ){
						total += countSize( files[ i ].listFiles() );
					} else {
						total += files[ i ].length();
					}
				}
				return total;
			}

			private void sendSize( long size, Socket socket ) throws IOException {
				DataOutputStream output = new DataOutputStream( new BufferedOutputStream( socket.getOutputStream() ) );
				output.writeLong( size );
				output.flush();
			}

			private void sendType( int type, Socket socket ) throws IOException {
				DataOutputStream output = new DataOutputStream( new BufferedOutputStream( socket.getOutputStream() ) );
				output.writeInt( type );
				output.flush();
			}

			private void sendFileSize( File file, Socket socket ) throws IOException {
				DataOutputStream output = new DataOutputStream( new BufferedOutputStream( socket.getOutputStream() ) );
				long size = file.length();
				output.writeLong( size );
				output.flush();
			}

			private void sendNumberOfFiles( int num, Socket socket ) throws IOException {
				DataOutputStream output = new DataOutputStream( new BufferedOutputStream( socket.getOutputStream() ) );
				output.writeInt( num );
				output.flush();
			}

			private JComponent createDescription(){
				JComponent description = Box.createVerticalBox();
				JLabel to = new JLabel( "To: " + buddy.getName() );
				to.setAlignmentX( JComponent.LEFT_ALIGNMENT );
				description.add( to );
				description.add( Box.createVerticalStrut( 3 ) );
				JLabel ip = new JLabel( "IP: " + buddy.getIP() + " Port: " + buddy.getPort() );
				ip.setAlignmentX( JComponent.LEFT_ALIGNMENT );
				description.add( ip );
				return description;
			}

			private boolean canSend( final Socket socket ) throws IOException {
				DataInputStream stream = new DataInputStream( socket.getInputStream() );
				byte b = stream.readByte();
				return b == YES;
			}

			private boolean sendFile( final JComponent box, File file, Socket socket, Lambda1 updateLambda, MutableBoolean canceled ) throws IOException {
				InputStream input = new DataInputStream( new BufferedInputStream( new FileInputStream( file ) ) );
				OutputStream output = new DataOutputStream( new BufferedOutputStream( socket.getOutputStream() ) );
				Lambda1 addComponent = new Lambda1(){
					public Object invoke( Object arg ){
						final JComponent jarg = (JComponent) arg;
						jarg.setAlignmentX( JComponent.LEFT_ALIGNMENT );
						SwingUtilities.invokeLater( new Runnable(){
							public void run(){
								box.add( jarg );
							}
						});
						return null;
					}
				};
				Transfer transfer = new Transfer( input, output, addComponent, file.getName(), file.length(), socket.getInetAddress().getHostAddress(), updateLambda, canceled );
				boolean done = transfer.transfer();
				output.flush();
				return done;
			}

			private void sendFileName( String filename, Socket socket ) throws IOException {

				DataOutputStream output = new DataOutputStream( new BufferedOutputStream( socket.getOutputStream() ) );

				/* Send the filename */
				new PrintStream( output ).print( filename );
				int total = 0;

				/* Then send the rest of the bits as filler, 1024 - size */
				for ( int i = filename.length(); i < FILENAME_SIZE; i++ ){
					total++;
					byte b = ' ';
					output.write( b );
				}
				// System.out.println( "Sent total of " + (filename.length() + total) + " bytes" );
				output.flush();
			}

			private List getSubFiles( File dir, String base ){
				List pairs = new ArrayList();

				File[] subs = dir.listFiles();
				for ( int i = 0; i < subs.length; i++ ){
					File file = subs[ i ];
					if ( file.isDirectory() ){
						pairs.add( new FilePair( file, base + file.getName() + "/" ) );
						pairs.addAll( getSubFiles( file, base + file.getName() + "/" ) );
					} else {
						pairs.add( new FilePair( file, base + file.getName() ) );
					}
				}

				return pairs;
			}

			private List getAllFiles( String base ){
				List pairs = new ArrayList();

				for ( int i = 0 ; i < files.length; i++ ){
					File file = files[ i ];
					if ( file.isDirectory() ){
						pairs.add( new FilePair( file, base + file.getName() + "/" ) );
						pairs.addAll( getSubFiles( file, base + file.getName() + "/" ) );
					} else {
						pairs.add( new FilePair( file, base + file.getName() ) );
					}
				}

				final List dirs = new ArrayList();
				final List files = new ArrayList();
				try{
					Lambda1.foreach( pairs, new Lambda1(){
						public Object invoke( Object o ){
							FilePair pair = (FilePair) o;
							if ( pair.isDirectory() ){
								dirs.add( pair );
							} else {
								files.add( pair );
							}
							return null;
						}
					});
				} catch ( Exception ex ){
				}

				List all = new ArrayList();
				all.addAll( dirs );
				all.addAll( files );

				return all;
			}

			public void run(){
				final int numberOfFiles = countFiles( files );
				final long totalSize = countSize( files );

				// System.out.println( "Sending " + numberOfFiles + " size = " + totalSize );
					
				final JComponent description = createDescription();
				description.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
				addUploadBar( description, buddy.getName() );
				// description.setBackground( new Color( 0, 250, 0 ) );
				final Color errorColor = new Color( 255, 0, 0 );
				final Lambda1 setTabColor = new Lambda1(){
					public Object invoke( Object o ){
						final int index = Reft.this.uploads.indexOfComponent( description );
						Reft.this.uploads.setForegroundAt( index, (Color) o );
						return null;
					}
				};

				SwingUtilities.invokeLater( new Runnable(){
					public void run(){
						setTabColor.invoke_( new Color( 0x888844 ) );
					}
				});

				final JLabel info = new JLabel();
				description.add( info );
				description.add( Box.createVerticalStrut( 3 ) );
				final MutableLong mutableLong = new MutableLong();
				final JProgressBar totalProgress = new JProgressBar( 0, 100 );
				final SpeedLabel speed = new SpeedLabel();
				final TimeLeft time = new TimeLeft( totalSize );
				final MutableLong updateTime = new MutableLong( System.currentTimeMillis() );
				final Lambda1 updateProgressLambda = new Lambda1(){
					public Object invoke( Object o ){
						mutableLong.add( ((Long) o).longValue() );
						/* only update a few times a second */
						if ( System.currentTimeMillis() - updateTime.getValue() > 80 ){
							long value = mutableLong.getValue();
							speed.update( value );
							time.update( value, speed.rawSpeed() );
							int newValue = (int)( value * 100.0 / totalSize);
							if ( newValue != totalProgress.getValue() ){
								totalProgress.setValue(  newValue );
								setTabColor.invoke_( new Color( 0, 32 + newValue * (255-64) / 100, 0 ) );
							}
							updateTime.setValue( System.currentTimeMillis() );
						}
						return null;
					}
				};
				
				final Box buttons = Box.createHorizontalBox();

				final Lambda0 addRemoveButton = new Lambda0(){ 

					public Object invoke(){
						SwingUtilities.invokeLater( new Runnable(){
							public void run(){
								JButton remove = new JButton( "Remove transfer" );
								remove.setToolTipText( "Removes this transfer window from the view" );
								buttons.add( Box.createHorizontalStrut( 5 ) );
								buttons.add( remove );
								remove.addActionListener( new ActionListener(){
									public void actionPerformed( ActionEvent action ){
										removeUploadBar( description );
									}
								});
							}
						});

						return null;
					}
				};

				final MutableBoolean canceled = new MutableBoolean( false );
				description.add( speed );
				description.add( time );
				description.add( totalProgress );
				description.add( Box.createVerticalStrut( 3 ) );
				JButton details = new JButton( "Show details" );
				details.setToolTipText( "Show more information about the transfer" );
				JButton cancel = new JButton( "Cancel" );
				cancel.setToolTipText( "Cancel this transfer" );
				buttons.add( details );
				buttons.add( Box.createHorizontalStrut( 5 ) );
				buttons.add( cancel );
				buttons.setAlignmentX( JComponent.LEFT_ALIGNMENT );

				cancel.addActionListener( new ActionListener(){
					public void actionPerformed( ActionEvent action ){
						canceled.setTrue();
						try{
							addRemoveButton.invoke();
						} catch ( Exception ex ){
						}
					}
				});

				description.add( buttons );
				description.add( Box.createVerticalStrut( 5 ) );
				final Box addMoreBox = Box.createVerticalBox();
				final JComponent addMore = new JScrollPane( addMoreBox );
				addMore.setVisible( false );
				description.add( addMore );
				details.addActionListener( new ActionListener(){
					private boolean set = false;
					public void actionPerformed( ActionEvent action ){
						set = ! set;
						addMore.setVisible( set );
						description.repaint();
					}
				});

				try{
					final Socket socket = new Socket( buddy.getIP(), buddy.getPort() );
					// System.out.println( "Send filename" );
					sendNumberOfFiles( numberOfFiles, socket );
					// System.out.println( "Send size" );
					sendSize( totalSize, socket );

					if ( canSend( socket ) ){
						try{
							List pairs = getAllFiles( "" );
							Lambda1.foreach( pairs, new Lambda1(){
								public Object invoke( Object o ) throws IOException {
										FilePair pair = (FilePair) o;
										System.out.println( "Sending " + pair.getName() );
										sendFileName( pair.getName(), socket );
										if ( pair.isDirectory() ){
											sendType( DIRECTORY, socket );
										} else {
											sendType( FILE, socket );
											sendFileSize( pair.getFile(), socket );
											sendFile( addMoreBox, pair.getFile(), socket, updateProgressLambda, canceled );
										}
									return null;
								}
							});
							System.out.println( "Done sending files" );
				
							SwingUtilities.invokeLater( new Runnable(){
								public void run(){
									if ( canceled.isTrue() ){
										info.setText( "Canceled!" );
									} else {
										info.setText( "Done!" );
										updateTime.setValue( 0 );
										mutableLong.setValue( 0 );
										updateProgressLambda.invoke_( new Long( totalSize ) );
									}
								}
							});
						} catch ( Exception e ){
							SwingUtilities.invokeLater( new Runnable(){
								public void run(){
									setTabColor.invoke_( errorColor );
									info.setText( "Error in transfer" );
								}
							});
							e.printStackTrace();
						}
					} else {
						SwingUtilities.invokeLater( new Runnable(){
							public void run(){
								setTabColor.invoke_( errorColor );
								info.setText( "Transfer denied" );
							}
						});
					}
					socket.close();
				} catch ( IOException ie ){
					ie.printStackTrace();
					SwingUtilities.invokeLater( new Runnable(){
						public void run(){
							setTabColor.invoke_( errorColor );
							info.setText( "Error in transfer" );
						}
					});
				}

				if ( canceled.isFalse() ){
					try{
						addRemoveButton.invoke();
					} catch ( Exception ex ){
					}
				} else {
					SwingUtilities.invokeLater( new Runnable(){
						public void run(){
							setTabColor.invoke_( errorColor );
						}
					});
				}
			}
		};

		thread.start();
	}

	private class FilePair{
		private String name;
		private File file;

		public FilePair( File file, String path ){
			this.name = path;
			this.file = file;
		}

		public String getName(){
			return this.name;
		}

		public boolean isDirectory(){
			return this.file.isDirectory();
		}

		public File getFile(){
			return file;
		}
	}

	public int getListenPort(){
		return listenPort;
	}

	public void setListenPort( int port ){
		this.listenPort = port;
	}

	public static LoadingWindow makeLoadingWindow( Object lock ){
		LoadingWindow w = new LoadingWindow( lock );
		Dimension size = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		w.setLocation( new Point( (int)(size.getWidth() / 2 - 100), (int)(size.getHeight() / 2 - 50) ) );

		return w;
	}

	private static void doGui(){
		try{
			// UIManager.setLookAndFeel( "com.sun.java.swing.plaf.gtk.GTKLookAndFeel" );
			// UIManager.setLookAndFeel( "com.sun.java.swing.plaf.gtk.GTKLookAndFeel" );
			/*
			MetalLookAndFeel m = new MetalLookAndFeel();
			m.setCurrentTheme( new OceanTheme() );
			// m.setCurrentTheme( new DefaultMetalTheme() );
			UIManager.setLookAndFeel( m );
			*/
		/*
		} catch ( ClassNotFoundException e ){
			e.printStackTrace();
		} catch ( InstantiationException e ){
			e.printStackTrace();
		} catch ( IllegalAccessException e ){
			e.printStackTrace();
		} catch ( javax.swing.UnsupportedLookAndFeelException e ){
			e.printStackTrace();
		}
		*/
		} catch ( Exception e ){
			e.printStackTrace();
		}

		Object lock = new Object();
		final LoadingWindow window = makeLoadingWindow( lock );

		window.setVisible( true );
		synchronized( lock ){
			window.setVisible( true );
			window.repaint();
			try{
				lock.wait();
			} catch ( Exception e ){
			}
		}

		/*
		try{
			SwingUtilities.invokeAndWait(
				new Runnable(){
					public void run(){
						System.out.println( "Set window visible" );
						window.setVisible( true );
						window.repaint();
						System.out.println( "Setup window" );
					}
				}
			);
		} catch ( InterruptedException ie ){
		} catch ( InvocationTargetException ie ){
		}
		*/

		try{
			final Reft reft = new Reft( window, lock );

			SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						window.setVisible( false );
						reft.setVisible( true );
					}
				}
			);
			/*
			synchronized( lock ){
				try{
					reft.repaint();
					lock.wait();
				} catch ( Exception e ){
				}
				window.setVisible( false );
			}
			*/
		} catch ( IOException ie ){
			ie.printStackTrace();
			window.setVisible( false );
		}

	}

	private static void showUsage(){
		System.out.println( "java -jar reft.jar [<options>]" );
		System.out.println( "<options> := <gui> | <http> | <ftp>" );
		System.out.println( "<gui> := " );
		System.out.println( "<http> := -http [<port>] [file1 file2...]" );
		System.out.println( "<ftp> := -ftp [<port>] [file1 file2...]" );
		System.out.println( "<port> := <int>" );
		System.out.println();
		System.out.println( "The arguments can be specified in any order." );
		System.out.println( "Default http port is 4050." );
		System.out.println( "Default ftp port is 4051." );
		System.out.println();
	}

    /* reduce to 2 decimal places */
	public static double reduce( double num ){
		return ((int)( num * 100 )) / 100.0;
	}

	public static String niceSize(double size){
        char sizes[] = new char[]{'b', 'k', 'm', 'g', 't', 'p', 'e', 'z', 'y'};
        double xsize = size;
        for (char c : sizes){
            if (xsize < 1024){
                return String.valueOf(reduce(xsize)) + c;
            }
            xsize /= 1024.0;
        }
        return String.valueOf(reduce(xsize)) + 'y';
	}

	private static void doHttpServer( final int port, final List<String> files ){
		int usePort = 4050;
		if ( port != -1 ){
			usePort = port;
		}
		Properties properties = new Properties();
		properties.setProperty( "port", String.valueOf( usePort ) );
		properties.setProperty( "logger.level", "info" );
		net.sf.ooweb.Server httpserver = new net.sf.ooweb.Server( new Configuration( properties ) );

		StringBuffer result = new StringBuffer();
		int num = 0;
		result.append( "<html><body>" );
		result.append( "<table>" );
		result.append( "<tr><td>Name</td><td>Type</td><td>Size</td></tr>" );
		for (String name : files){
			result.append( "<tr>" );
			result.append( "<td>" );
			result.append( "<a href='/file?id=" + num + "'>" + name + "</a>" );
			result.append( "</td><td>File</td>" );
			result.append( "<td>" + niceSize( new File( name ).length() ) + "</td>" );
			result.append( "</tr>" );
			num++;
		}

		result.append( "</table>" );

		result.append( "</body></html>" );

		final String listing = result.toString();

		httpserver.register( "/", new Object(){
			
			public String index(){
				return listing;
			}

			/* Returns a file. Called when the url is like
			 * http://whatever/foo/file?id=4
			 * Actually the parameter, id, can be an arbitrary name
			 * because ooweb will apply file to the arguments regardless
			 * of their name
			 * (let ((args (parse-query-string)))
			 *   (apply file args))
			 */
			public Object file( String id ){
				HTTP.setMimeType( "application/octet-stream" );
				try{
					int i = Integer.parseInt( id );
					FileTreeNode f = new FileTreeNode( new File( (String) files.get( i ) ) );
					return f;
				} catch ( Exception e ){
					e.printStackTrace();
				}
				return null;
			}
		});

		try{
			httpserver.startUp();
			httpserver.listen();
		} catch ( IOException ie ){
			ie.printStackTrace();
		}
	}

	private static void doFtpServer( int port, List files ){
		int usePort = 4051;
		if ( port != -1 ){
			usePort = port;
		}
		final FileTree manager = new FileTree();
		File[] all = new File[ files.size() ];
		int num = 0;
		for ( Iterator it = files.iterator(); it.hasNext(); ){
			all[ num ] = new File( (String) it.next() );
			num += 1;
		}
		addFiles( (FileTreeNode) manager.getRoot(), all, new Lambda0(){
			public Object invoke(){
				return Boolean.TRUE;
			}
		}, new Lambda2(){
			public Object invoke( Object node, Object children ){
				manager.insert( (TreeNode) node, (int[]) children );
				return null;
			}
		}, 0 );

		try{
			Properties properties = new Properties();
			rheise.jftpd.Server ftpserver = new rheise.jftpd.Server( properties, usePort );
			System.out.println( "Starting ftp server on port " + usePort );
			ftpserver.start( manager, new Lambda1(){
				public Object invoke( Object o ){
					System.out.println( o.toString() );
					return null;
				}
			});
		} catch ( rheise.jftpd.FTPException f ){
			f.printStackTrace();
		}
	}

	private static List<String> filterFiles( List things ){
		List<String> files = new ArrayList();
		for ( Iterator it = things.iterator(); it.hasNext(); ){
			String s = (String) it.next();
			if ( new File( s ).isFile() ){
				files.add( s );
			}
		}
		return files;
	}

	public static void main( String[] args ){

		final int GUI = 0;
		final int HTTP_SERVER = 1;
		final int FTP_SERVER = 2;
		int mode = GUI;

		List extraArgs = new ArrayList();
		int port = -1;
		for ( int i = 0; i < args.length; i++ ){
			if ( args[ i ].equals( "-http" ) ){
				mode = HTTP_SERVER;
			} else if ( args[ i ].equals( "-ftp" ) ){
				mode = FTP_SERVER;
			} else if ( args[ i ].equals( "-port" ) ){
				i += 1;
				if ( i < args.length ){
					port = Integer.parseInt( args[ i ] );
				}
			} else {
				extraArgs.add( args[ i ] );
			}
		}

		showUsage();

		switch( mode ){
			case GUI : {
				doGui();
				break;
			}
			case HTTP_SERVER : {
				doHttpServer( port, filterFiles( extraArgs ) );
				break;
			}
			case FTP_SERVER : {
				doFtpServer( port, filterFiles( extraArgs ) );
				break;
			}
		}
	}
}

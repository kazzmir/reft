package com.rafkind.reft;

import java.util.Vector;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.Point;
import java.awt.FlowLayout;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import javax.swing.JFileChooser;

import java.awt.Component;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JDialog;
import javax.swing.Box;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;

import javax.swing.Popup;
import javax.swing.PopupFactory;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.swixml.SwingEngine;

public class BuddyList{

	private Vector memberList;
	private JList people;

	private BuddyList( JList people ){
		this.people = people;
		try{
			memberList = (Vector) loadObject( "memberlist.obj" );
		} catch ( IOException ie ){
			memberList = new Vector();
			memberList.add( new Buddy( "Me!", "127.0.0.1", Reft.DEFAULT_REFT_PORT ) );
		}
		this.people.setListData( memberList );
	}

	public Vector getMemberList(){
		return this.memberList;
	}

	public static Component createBuddyList( final Lambda2 sendFileLambda ){

		SwingEngine engine = new SwingEngine( "buddylist.xml" );
		
		final JPanel root = (JPanel) engine.getRootComponent();

		// people = new JList( memberList );
		final JList people = (JList) engine.find( "people" );
		// people.setListData( memberList );

		final BuddyList buddyList = new BuddyList( people );
		final Vector memberList = buddyList.getMemberList();

		final JPanel currentBuddy = (JPanel) engine.find( "currentbuddy" );
		// final Box buddies = Box.createVerticalBox();

		final JTabbedPane sentFiles = (JTabbedPane) engine.find( "sentfiles" );
		// sentFiles.setMaximumSize( new Dimension( 650, 300 ) );
		final Lambda2 mySendFiles = new Lambda2(){
			public Object invoke( Object buddy, Object o ){
				try{
					File[] files = (File[]) o;
					sentFiles.add( new FileList( buddyList, files, sendFileLambda ) );
					sendFileLambda.invoke( buddy, files );
				} catch ( Exception ex ){
				}
				return null;
			}
		};

		final Lambda1 updateCurrentBuddy = new Lambda1(){
			public Object invoke( Object b ){
				Buddy buddy = (Buddy) b;
				currentBuddy.removeAll();

				Box box = Box.createVerticalBox();
				box.add( new JLabel( "Buddy: " + buddy.getName() ) );
				box.add( Box.createVerticalStrut( 5 ) );
				box.add( new JLabel( "IP/Host: " + buddy.getIP() ) );
				box.add( Box.createVerticalStrut( 5 ) );
				box.add( new JLabel( "Port: " + buddy.getPort() ) );

				currentBuddy.add( box );
				box.revalidate();

				return null;
			}
		};

		people.addMouseListener( new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if ( e.getClickCount() == 2 ){
					int index = people.locationToIndex( e.getPoint() );
					Buddy buddy = (Buddy) memberList.get( index );
					processBuddy( root, buddyList, buddy, updateCurrentBuddy, mySendFiles );
				}
			}
		});

		root.setBorder( BorderFactory.createBevelBorder( BevelBorder.RAISED ) );

		// this.add( engine.getRootComponent() );
		// buddies.add( new JLabel( "Buddy List" ) );
		
		JToolBar buttons = (JToolBar) engine.find( "toolbar" );
		// buttons.setFloatable( false );

		// buddies.add( buttons );

		/*
		JScrollPane scrolly = new JScrollPane( people );
		scrolly.setMaximumSize( new Dimension( 200, 400 ) );
		*/
		// buddies.add( scrolly );

		// final BuddyList list = this;
		JButton add = (JButton) engine.find( "add" );
		add.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent e ){
				makeBuddyDialog( root, buddyList );
			}
		});

		JButton send = (JButton) engine.find( "send" );
		send.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent e ){
				Buddy[] buddies = buddyList.getAllBuddies();
				if ( buddies.length > 0 ){
					sendFile( root, buddies, mySendFiles );
				}
			}
		});

		JButton remove = (JButton) engine.find( "remove" );
		remove.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent e ){
				Buddy buddy = buddyList.getCurrentBuddy();
				if ( buddy != null ){
					buddyList.removeBuddy( buddy );
				}
			}
		});
		
		JButton edit = (JButton) engine.find( "edit" );
		edit.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent e ){
				Buddy buddy = buddyList.getCurrentBuddy();
				if ( buddy != null ){
					processBuddy( root, buddyList, buddy, updateCurrentBuddy, mySendFiles );
				}
			}
		});

		// buddies.add( currentBuddy );

		people.addListSelectionListener( new ListSelectionListener(){
			public void valueChanged( ListSelectionEvent event ){
				try{
					Buddy buddy = buddyList.getCurrentBuddy();
					if ( buddy != null ){
						updateCurrentBuddy.invoke( buddy );
					}
				} catch ( Exception e ){
				}
			}
		});

		// buddies.add( sentFiles );
		return engine.getRootComponent();
	}

	private static class FileList extends JPanel {
		public FileList( final BuddyList buddyList, final File[] files, final Lambda2 sendFileLambda ){

			Box box = Box.createVerticalBox();
			this.add( box );

			JButton button = new JButton( "Send these files" );
			button.addActionListener( new AbstractAction(){
				public void actionPerformed( ActionEvent e ){
					try{
						Buddy[] buddies = buddyList.getAllBuddies();
						for ( int i = 0; i < buddies.length; i++ ){
							sendFileLambda.invoke( buddies[ i ], files );
						}
					} catch ( Exception ex ){
					}
				}
			});

			box.add( button );
			box.add( Box.createVerticalStrut( 5 ) );

			JScrollPane pane = new JScrollPane( new JList( files ) );
			pane.setMaximumSize( new Dimension( 600, 200 ) );
			box.add( pane );
			box.add( Box.createVerticalStrut( 5 ) );
		}
	}

	private Object loadObject( String name ) throws IOException {
		try{
			ObjectInputStream stream = new ObjectInputStream( new FileInputStream( name ) );
			return stream.readObject();
		} catch ( ClassNotFoundException ce ){
			throw new IOException( "Could not find class" );
		}
	}

	private void writeObject( Object object, String file ) throws IOException {
		ObjectOutputStream stream = new ObjectOutputStream( new FileOutputStream( file ) );
		stream.writeObject( object );
	}

	private Buddy[] getAllBuddies(){
		int[] indexes = people.getSelectedIndices();
		Buddy[] buddies = new Buddy[ indexes.length ];
		for ( int i = 0; i < indexes.length; i++ ){
			buddies[ i ] = (Buddy) memberList.get( indexes[ i ] );
		}
		return buddies;
	}

	private Buddy getCurrentBuddy(){
		int index = people.getSelectedIndex();
		if ( index != -1 ){
			return (Buddy) memberList.get( index );
		} else {
			return null;
		}
	}

	private void removeBuddy( Buddy buddy ){
		int index = memberList.indexOf( buddy );
		if ( index != -1 ){
			memberList.remove( index );
			people.setListData( memberList );
			try{
				writeObject( memberList, "memberlist.obj" );
			} catch ( IOException ie ){
				System.out.println( "Could not write member list" );
				ie.printStackTrace();
			}
		}
	}

	private static void sendFile( final JPanel root, final Buddy[] buddies, final Lambda2 sendFileLambda ){
		/*
		JFileChooser chooser = new JFileChooser();
		int returnVal = chooser.showOpenDialog( this );
		if ( returnVal == JFileChooser.APPROVE_OPTION ){
			for ( int i = 0; i < buddies.length; i++ ){
				this.sendFileLambda.invoke( buddies[ i ], chooser.getSelectedFile() );
			}
		}
		*/
		final Lambda1 send = new Lambda1(){
			public Object invoke( Object arg ){
				File[] files = (File[]) arg;
				try{
					for ( int i = 0; i < buddies.length; i++ ){
						sendFileLambda.invoke( buddies[ i ], files );
					}
				} catch ( Exception e ){
				}
				return null;
			}
		};

		try{
			JDialog fileDialog = new FileCollectorDialog( send );
			fileDialog.setLocation( root.getParent().getLocationOnScreen() );
			fileDialog.setSize( 700, 400 );
			fileDialog.setVisible( true );
		} catch ( Exception e ){
		}
	}

	private static class ProcessBuddyDialog extends JDialog {
		public ProcessBuddyDialog( final JPanel root, final BuddyList buddyList, final Buddy buddy, final Lambda1 updateBuddy, final Lambda2 sendFileLambda ){
			this.setSize( 500, 200 );

			final JDialog lambdaDialog = this;
			final Lambda0 closeDialog = new Lambda0(){
				public Object invoke(){
					lambdaDialog.dispose();
					return null;
				}
			};

			JPanel panel = new JPanel();
			this.getContentPane().add( panel );

			Box layout = Box.createVerticalBox();
			panel.add( layout );
			layout.setAlignmentY( 0 );

			final JLabel errorMessage = new JLabel();
			layout.add( errorMessage );
			layout.add( Box.createVerticalStrut( 2 ) );

			final JTextField name = new JTextField( buddy.getName(), 5 );
			name.setEditable( false );
			Lambda0 changeName = new Lambda0(){
				public Object invoke(){
					if ( name.getText().equals( "" ) ){
						errorMessage.setText( "Cannot have empty name" );
					} else {
						buddy.setName( name.getText() );
						try{
							updateBuddy.invoke( buddy );
						} catch ( Exception e ){
						}
					}
					return null;
				}
			};

			layout.add( createEditableField( "Name", name, changeName ) );
			layout.add( Box.createVerticalStrut( 5 ) );

			final JTextField ip = new JTextField( buddy.getIP(), 15 );
			ip.setEditable( false );
			Lambda0 changeIP = new Lambda0(){
				public Object invoke(){
					if ( ip.getText().equals( "" ) ){
						errorMessage.setText( "Cannot have empty IP" );
					} else {
						buddy.setIP( ip.getText() );
						try{
							updateBuddy.invoke( buddy );
						} catch ( Exception e ){
						}
					}
					return null;
				}
			};

			layout.add( createEditableField( "IP/Host", ip, changeIP ) );
			layout.add( Box.createVerticalStrut( 5 ) );

			final JTextField port = new JTextField( String.valueOf( buddy.getPort() ), 5 );
			port.setEditable( false );
			Lambda0 changePort = new Lambda0(){
				public Object invoke(){
					try{
						buddy.setPort( Integer.parseInt( port.getText() ) );
						updateBuddy.invoke( buddy );
					} catch ( NumberFormatException ne ){
						errorMessage.setText( "Invalid port: " + ne.getMessage() );
					} catch ( Exception ex ){
					}
					return null;
				}
			};

			layout.add( createEditableField( "Port", port, changePort ) );
			layout.add( Box.createVerticalStrut( 5 ) );

			Box buttons = Box.createHorizontalBox();
			layout.add( buttons );

			JButton send = new JButton( "Send a file" );
			buttons.add( send );
			buttons.add( Box.createHorizontalStrut( 5 ) );
			send.addActionListener( new ActionListener(){
				public void actionPerformed( ActionEvent e ){
					try{
						closeDialog.invoke();
						sendFile( root, new Buddy[]{ buddy }, sendFileLambda );
					} catch ( Exception ex ){
					}
				}
			});

			JButton removeBuddy = new JButton( "Remove this buddy" );
			buttons.add( removeBuddy );
			buttons.add( Box.createHorizontalStrut( 5 ) );
			removeBuddy.addActionListener( new ActionListener(){
				public void actionPerformed( ActionEvent action ){
					try{
						buddyList.removeBuddy( buddy );
						closeDialog.invoke();
					} catch ( Exception ex ){
					}
				}
			});

			JButton closeWindow = new JButton( "Close Window" );
			buttons.add( closeWindow );
			buttons.add( Box.createHorizontalStrut( 5 ) );

			CloseHook close = new CloseHook( closeDialog );
			this.addWindowListener( close );
			closeWindow.addActionListener( new ActionListener(){
				public void actionPerformed( ActionEvent action ){
					try{
						closeDialog.invoke();
					} catch ( Exception ex ){
					}
				}
			});

		}

		private JComponent createEditableField( String name, final JTextField text, final Lambda0 lambda ){
			final JButton button = new JButton( "Edit" );
			Box layout = Box.createHorizontalBox();

			layout.add( new JLabel( name ) );
			layout.add( Box.createHorizontalStrut( 5 ) );
			layout.add( text );
			layout.add( Box.createHorizontalStrut( 5 ) );
			layout.add( button );

			button.addActionListener( new ActionListener(){
				private boolean set = false;
				public void actionPerformed( ActionEvent action ){
					try{
						if ( set ){
							lambda.invoke();	
							text.setEditable( false );
							button.setText( "Edit" );
							set = false;
						} else {
							set = true;
							text.setEditable( true );
							button.setText( "Keep" );
						}
					} catch ( Exception e ){
					}
				}
			});

			return layout;
		}
	}
	
	private static void processBuddy( JPanel root, BuddyList buddyList, Buddy b, Lambda1 updateBuddy, Lambda2 sendFileLambda ){
		ProcessBuddyDialog process = new ProcessBuddyDialog( root, buddyList, b, updateBuddy, sendFileLambda );
		Point location = root.getLocationOnScreen();
		process.setLocation( location );
		process.setVisible( true );
	}

	private void addBuddy( String name, String ip, int port ){
		memberList.add( new Buddy( name, ip, port ) );
		people.setListData( memberList );

		try{
			writeObject( memberList, "memberlist.obj" );
		} catch ( IOException ie ){
			System.out.println( "Could not write member list" );
			ie.printStackTrace();
		}
	}

	private static class AddBuddyDialog extends JDialog {

		public AddBuddyDialog( final BuddyList buddyList ){
			super();

			this.setSize( 300, 200 );

			final JDialog lambdaDialog = this;
			final Lambda0 closeDialog = new Lambda0(){
				public Object invoke(){
					lambdaDialog.dispose();
					return null;
				}
			};

			CloseHook close = new CloseHook( closeDialog );
			this.addWindowListener( close );

			final Box layout = Box.createVerticalBox();
			layout.setAlignmentY( 0 );

			final JLabel errorMessage = new JLabel();
			layout.add( errorMessage );
			errorMessage.setVisible( false );

			final JTextField name = new JTextField( 5 );
			layout.add( createField( "Name", name ) );
			layout.add( Box.createVerticalStrut( 10 ) );
			final JTextField ip = new JTextField( 5 );
			layout.add( createField( "IP/Host", ip ) );
			layout.add( Box.createVerticalStrut( 10 ) );
			final JTextField port = new JTextField( "4949", 5 );
			layout.add( createField( "Port", port ) );

			layout.add( Box.createVerticalStrut( 15 ) );
			
			Box buttons = Box.createHorizontalBox();
			JButton addButton = new JButton( "Add Buddy" );
			buttons.add( addButton );
			buttons.add( Box.createHorizontalStrut( 5 ) );

			addButton.addActionListener( new ActionListener(){
				public void actionPerformed( ActionEvent action ){

					try{
						String _name = name.getText();
						if ( _name.equals( "" ) ){
							throw new Exception( "Enter a name" );
						}
						String _ip = ip.getText();
						if ( _ip.equals( "" ) ){
							throw new Exception( "Enter an IP address" );
						}
						int _port = 4949;
						Integer.parseInt( port.getText() );
						buddyList.addBuddy( _name, _ip, _port );
						closeDialog.invoke();
					} catch ( Exception e ){
						errorMessage.setText( "Error: " + e.getMessage() );
						errorMessage.setVisible( true );
						layout.doLayout();
					}
				}
			});

			JButton closeButton = new JButton( "Close Window" );
			buttons.add( closeButton );
			closeButton.addActionListener( new ActionListener(){
				public void actionPerformed( ActionEvent e ){
					try{
						closeDialog.invoke();
					} catch ( Exception ex ){
					}
				}
			});

			layout.add( buttons );

			// layout.add( Box.createGlue() );

			JPanel pane = new JPanel();
			pane.setMaximumSize( new Dimension( 30, 10 ) );
			pane.add( layout );
			this.getContentPane().add( pane );
		}
	}

	private static JComponent createField( String label, JComponent field ){
		Box box = Box.createHorizontalBox();
		box.add( new JLabel( label ) );
		box.add( Box.createHorizontalStrut( 5 ) );
		box.add( field );
		return box;
	}

	private static void makeBuddyDialog( JPanel root, BuddyList buddyList ){
		AddBuddyDialog add = new AddBuddyDialog( buddyList );
		Point location = root.getLocationOnScreen();
		add.setLocation( location );
		add.setVisible( true );
	}
}

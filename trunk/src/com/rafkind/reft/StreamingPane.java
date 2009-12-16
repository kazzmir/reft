package com.rafkind.reft;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.ActionEvent;

import java.io.File;
import java.io.IOException;

import java.awt.Color;
import java.awt.Component;

import java.util.Properties;

import org.swixml.SwingEngine;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileFilter;
import javax.swing.ListCellRenderer;

import com.rafkind.reft.stream.StreamReader;
import com.rafkind.reft.stream.StreamingInputStream;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.sf.ooweb.Configuration;
import net.sf.ooweb.HTTP;

public class StreamingPane{

	private JPanel root;

	private SimpleDateFormat date = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
	private String currentTime(){
		return date.format( Calendar.getInstance().getTime() );
	}
	
	public StreamingPane( final Reft reft ){
		SwingEngine engine = new SwingEngine( "stream.xml" );
		root = (JPanel) engine.getRootComponent();

		final JTextField serverNameField = (JTextField) engine.find( "server" );
		serverNameField.setToolTipText( "Set this field to make the url easier to copy/paste" );
		serverNameField.setText( "127.0.0.1" );
		final int defaultPort = 5000;
		final JTextField portField = (JTextField) engine.find( "port" );
		portField.setText( String.valueOf( defaultPort ) );

		final JTextArea logs = (JTextArea) engine.find( "log" );
		final StringBuffer messages = new StringBuffer();
		
		final JTextField url = (JTextField) engine.find( "url" );
		url.setToolTipText( "Copy this url into a media player, not a browser" );

		final Lambda0 setUrl = new Lambda0(){
			public Object invoke(){
				url.setText( "http://" + serverNameField.getText() + ":" + portField.getText() + "/" );
				return null;
			}
		};

		final Lambda1 log = new Lambda1(){
			public Object invoke( Object message ){
				messages.append( currentTime() ).append( " " );
				messages.append( (String) message ).append( "\n" );
				logs.setText( messages.toString() );
				return null;
			}
		};

		setUrl.invoke_();

		serverNameField.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent ae ){
				setUrl.invoke_();
			}
		});

		portField.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent ae ){
				setUrl.invoke_();
			}
		});

		Properties properties = new Properties();
		properties.setProperty( "logger.level", "debug" );
		properties.setProperty( "port", String.valueOf( defaultPort ) );
		final net.sf.ooweb.Server server = new net.sf.ooweb.Server( new Configuration( properties ) );

		final JList list = (JList) engine.find( "fileList" );
		final FileList files = new FileList();
		list.setModel(files);
		list.setCellRenderer( new ListCellRenderer(){
			public Component getListCellRendererComponent(
					  JList list,
					  Object value,
					  int index,
					  boolean isSelected,
					  boolean cellHasFocus ) {

				JLabel label = new JLabel();
				label.setOpaque( true );
				label.setText( value.toString() );
				Color background = Color.white;
				// System.out.println( "Index: " + index + " Current " + files.getCurrentSong() + " selected " + isSelected );
				if ( index == files.getCurrentSong() - 1 ){
					if ( isSelected ){
						background = new Color( 128, 64, 12 );
					} else {
						background = new Color( 150, 10, 20 );
					}
				} else {
					if ( isSelected ){
						background = new Color( 0, 128, 255 );
					} else {
						background = Color.white;
					}
				}
				label.setBackground( background );
				label.setForeground( Color.black );
				return label;
			}
		});

        final JTextField nowPlaying = (JTextField) engine.find("now-playing");
        final JLabel timePosition = (JLabel) engine.find("time-position");

		final StreamReader reader = new StreamReader(new Lambda0(){
			private Lambda0 next = files.getNextFileLambda();
			public Object invoke(){
				File f = (File) next.invoke_();
				list.repaint();
                nowPlaying.setText(f.getName());
				log.invoke_( "Next song " + f.getName() );
				return f;
			}
		}, new Lambda1(){
            private String pad(int f){
                if (f < 10){
                    return "0" + f;
                }
                return String.valueOf(f);
            }

            public Object invoke(Object o_){
                int time = (Integer) o_;
                int minutes = time / 60;
                int seconds = time % 60;
                timePosition.setText(minutes + ":" + pad(seconds));
                return null;
            }
        });

		final MutableBoolean serverIsOn = new MutableBoolean( false );
		final JRadioButton on = (JRadioButton) engine.find( "on" );
		final JRadioButton off = (JRadioButton) engine.find( "off" );

		off.setSelected( true );

		on.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent ae ){
				if ( serverIsOn.isFalse() && ! files.getList().isEmpty() ){
					try{
							int i = Integer.parseInt( portField.getText() );
							serverIsOn.setFalse();
							off.setSelected( true );
							while (reader.isRunning()){
								rest(10);
							}
							reader.reset();
							reader.setDone(false);
							new Thread(reader).start();
							server.register( "/", new Object(){
								public Object index() throws IOException {
									HTTP.setMimeType( "audio/mpeg" );
									log.invoke_( "Adding listener " + Thread.currentThread().getName() );
									return new StreamingInputStream(reader);
								}
								
								public String handleError( String code, String message ){
									log.invoke_( "Removing listener " + Thread.currentThread().getName() );
									return "Use a media player to listen to this stream.";
								}
							});
							server.setPort( i );
							server.startUp();
							new Thread(){
								public void run(){
									server.listen();
								}
							}.start();
							serverIsOn.setTrue();
							on.setSelected( true );
							log.invoke_( "Started streaming HTTP server on port " + portField.getText() );
					} catch ( NumberFormatException ne ){
						System.out.println( "Couldn't restart streaming HTTP server on port '" + portField.getText() + "'" );
						log.invoke_( "Could not start streaming HTTP server on port " + portField.getText() );
					} catch ( IOException ie ){
						ie.printStackTrace();
						log.invoke_( "Could not start streaming HTTP server on port " + portField.getText() );
					}
				} else {
					off.setSelected( true );
					if ( files.getList().isEmpty() ){
						log.invoke_( "You must add some files before starting the server" );
					}
				}
			}
		});

		off.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent ae ){
                if ( serverIsOn.isTrue() ){
                    serverIsOn.setFalse();
                    reader.setDone( true );
                    server.shutdown();
                    off.setSelected( true );
                    nowPlaying.setText("Server is off!");
                    log.invoke_( "Stopped streaming HTTP server" );
                }
			}
		});

		final JButton add = (JButton) engine.find( "add" );
		final JButton remove = (JButton) engine.find( "delete" );

		add.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent ae ){
				JFileChooser chooser = new JFileChooser( new File( "." ) );
				chooser.setFileFilter( new FileFilter(){
					public boolean accept( File f ){
						return f.isDirectory() || f.getName().endsWith( ".mp3" );
					}

					public String getDescription(){
						return "MP3 files";
					}
				});
				chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
				chooser.setMultiSelectionEnabled( true );
				int returnVal = chooser.showOpenDialog( reft );
				if ( returnVal == JFileChooser.APPROVE_OPTION ){
					final File[] myfiles = chooser.getSelectedFiles();
					for ( int i = 0; i < myfiles.length; i++ ){
						if ( myfiles[ i ].isDirectory() ){
						}
						files.add( myfiles[ i ] );
					}
		 		}
			}
		});

		remove.addActionListener( new AbstractAction(){
			public void actionPerformed( ActionEvent ae ){
				files.removeAll( list.getSelectedValues() );
			}
		});

		list.addKeyListener( new KeyAdapter(){
			public void keyTyped( KeyEvent e ){
				if ( e.getKeyChar() == KeyEvent.VK_DELETE ){
					files.removeAll( list.getSelectedValues() );
				}
			}
		});

		list.addMouseListener( new MouseAdapter() {
			public void mouseClicked( MouseEvent clicked ){
				if ( clicked.getClickCount() == 2 ){
					int index = list.locationToIndex( clicked.getPoint() );
					File f = (File) files.getElementAt( index );
					try{
							  reader.setFile( f );
							  files.setCurrentSong( index );
							  log.invoke_( "Switch to " + f.getName() );
					} catch ( IOException ie ){
						  log.invoke_( "Could not open " + f.getName() + " because " + ie.getMessage() );
					}
			  }
		  }
		});
	}

	public JPanel getPanel(){
		return root;
	}

	private void rest( int n ){
		try{
			Thread.sleep( n );
		} catch ( Exception i ){
		}
	}
}

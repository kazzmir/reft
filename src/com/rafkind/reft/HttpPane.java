package com.rafkind.reft;

import javax.swing.AbstractAction;

import javax.swing.SwingUtilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSlider;

import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;

import java.awt.Color;

import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;

import javax.swing.JFileChooser;

import org.swixml.SwingEngine;

import java.awt.event.ActionEvent;

import java.util.List;
import java.util.Iterator;
import java.util.Properties;

import java.io.IOException;
import java.io.File;

import net.sf.ooweb.Configuration;
import net.sf.ooweb.HTTP;

public class HttpPane{

    private static final Integer LogDebug = new Integer( 3 );
    private static final Integer LogInfo = new Integer( 2 );
    private static final Integer LogImportant = new Integer( 1 );
    private static final Integer LogNone = new Integer( 0 );

    private net.sf.ooweb.Server httpserver;

    private JPanel panel;

    public JPanel getPanel(){
        return panel;
    }

    private SimpleDateFormat date = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
    private String currentTime(){
        return date.format( Calendar.getInstance().getTime() );
    }

    private String logToDescription( int i ){
        if ( i == LogDebug.intValue() ){
            return "Log everything";
        }
        if ( i == LogInfo.intValue() ){
            return "Log most things";
        }
        if ( i == LogImportant.intValue() ){
            return "Log important things";
        }
        if ( i == LogNone.intValue() ){
            return "Log nothing";
        }
        return "Log";
    }

    public HttpPane(){
        SwingEngine engine = new SwingEngine( "http.xml" );
        this.panel = (JPanel) engine.getRootComponent();

        final JTextField serverNameField = (JTextField) engine.find( "server" );
        final JTextField httpPortField = (JTextField) engine.find( "port" );

        final StringBuffer messages = new StringBuffer();
        final JTextArea logs = (JTextArea) engine.find( "log" );
        final String persistFilesName = "httpFiles.obj";
        final JLabel logDescription = (JLabel) engine.find( "log-level-desc" );
        final JSlider logLevel = (JSlider) engine.find( "log-level" );
        // final JList fileList = (JList) engine.find( "fileList" );
        // final Vector files = (Vector) loadObject( persistFilesName, new Vector() );

        final JTree files = (JTree) engine.find( "fileList" );
        final FileTree fileTree = new FileTree();
        files.setModel( fileTree );

        // fileList.setListData( files );

        files.addKeyListener( new KeyAdapter(){
            public void keyTyped( KeyEvent e ){
                if ( e.getKeyChar() == KeyEvent.VK_DELETE ){
                    // System.out.println( "Delete something" );
                    TreePath[] selected = files.getSelectionPaths();
                    if ( selected != null ){
                        fileTree.remove( selected );
                    }
                }
            }
        });

        final JRadioButton on = (JRadioButton) engine.find( "on" );
        final JRadioButton off = (JRadioButton) engine.find( "off" );
        off.setSelected( true );

        final MutableBoolean serverIsOn = new MutableBoolean( false );

        final MutableLong currentLogLevel = new MutableLong( LogDebug.intValue() );

        logLevel.setValue( (int) currentLogLevel.getValue() );
        logDescription.setText( logToDescription( logLevel.getValue() ) );
        logLevel.addChangeListener( new ChangeListener(){
            public void stateChanged( ChangeEvent e ){
                currentLogLevel.setValue( logLevel.getValue() );
                logDescription.setText( logToDescription( logLevel.getValue() ) );
            }
        });

        final Lambda1 okLogLevel = new Lambda1(){
            public Object invoke( Object level ){
                int i = ((Integer) level).intValue();
                return Boolean.valueOf( currentLogLevel.getValue() >= i );
            }
        };

        final Lambda2 log = new Lambda2(){
            public Object invoke( Object level, Object message ){
                if ( Boolean.TRUE.equals( okLogLevel.invoke_( level ) ) ){
                    messages.append( currentTime() ).append( " " );
                    messages.append( (String) message ).append( "\n" );
                    logs.setText( messages.toString() );
                }
                return null;
            }
        };

        on.addActionListener( new AbstractAction(){
            public void actionPerformed( ActionEvent ae ){
                if ( serverIsOn.isFalse() ){
                    try{
                        try{
                            int i = Integer.parseInt( httpPortField.getText() );
                            serverIsOn.setFalse();
                            off.setSelected( true );
                            startHTTPServer( fileTree, i, log );
                            serverIsOn.setTrue();
                            on.setSelected( true );

                            log.invoke( LogInfo, "Started HTTP server on port " + httpPortField.getText() );
                        } catch ( NumberFormatException ne ){
                            System.out.println( "Couldn't restart HTTP server on port '" + httpPortField.getText() + "'" );
                            log.invoke( LogImportant, "Could not start HTTP server on port " + httpPortField.getText() );
                        } catch ( IOException ie ){
                            ie.printStackTrace();
                            log.invoke( LogImportant, "Could not start HTTP server on port " + httpPortField.getText() );
                        }
                    } catch ( Exception e ){
                        e.printStackTrace();
                    }
                }
            }
        });

        /* turn off the http server */
        off.addActionListener( new AbstractAction(){
            public void actionPerformed( ActionEvent ae ){
                if ( serverIsOn.isTrue() ){
                    stopHTTPServer();
                    serverIsOn.setFalse();
                    log.invoke_( LogInfo, "HTTP Server is off" );
                }
            }
        });

        final JTextField browse = (JTextField) engine.find( "browse" );

        final Lambda0 setBrowse = new Lambda0(){
            public Object invoke(){
                browse.setText( "http://" + serverNameField.getText() + ":" + httpPortField.getText() + "/" );
                return null;
            }
        };

        httpPortField.setText( "4050" );
        serverNameField.setText( "localhost" );
        final JTextField url = (JTextField) engine.find( "url" );

        final MutableBoolean keepAdding = new MutableBoolean();

        final JButton addFile = (JButton) engine.find( "add" );
        final JButton addDir = (JButton) engine.find( "add_dir" );
        final JButton deleteFile = (JButton) engine.find( "delete" );
        final JButton stop = (JButton) engine.find( "stop" );

        addFile.setToolTipText( "<html>This button will bring up a new window that lets you select files to share over HTTP.<br>Be sure to turn the server on to actually share them.</html>" );

        deleteFile.setToolTipText( "Remove all selected files from the list above." );

        addDir.setToolTipText( "Add a new sub-folder. This is useful for organizing many files into a single logical place." );

        stop.setToolTipText( "When many files are being added this button lets you stop the addition process" );

        stop.addActionListener( new AbstractAction(){
            public void actionPerformed( ActionEvent event ){
                keepAdding.setFalse();
            }
        });

        addDir.addActionListener( new AbstractAction(){
            private FileTreeNode addFolder( FileTreeNode addTo ){
                FileTreeNode node = new FileTreeNode( "type directory here", true );
                addTo.add( node );
                int[] children = new int[ 1 ];
                children[ 0 ] = addTo.getChildCount() - 1;
                fileTree.insert( addTo, children );
                return node;
            }

            public void actionPerformed( ActionEvent event ){
                final TreePath path = files.getSelectionPath();
                if ( path != null ){
                    FileTreeNode node = (FileTreeNode) path.getLastPathComponent();
                    if ( node.isDirectory() ){
                        FileTreeNode dir = addFolder( (FileTreeNode) path.getLastPathComponent() );
                        TreePath update = path.pathByAddingChild( dir );
                        files.scrollPathToVisible( update );
                        files.startEditingAtPath( update );
                    }
                }
            }
        });

        addFile.addActionListener( new AbstractAction(){
            public void actionPerformed( ActionEvent event ){
                JFileChooser chooser = new JFileChooser( new File( "." ) );
                chooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
                chooser.setMultiSelectionEnabled( true );
                int returnVal = chooser.showOpenDialog( getPanel() );
                if ( returnVal == JFileChooser.APPROVE_OPTION ){
                    final File[] myfiles = chooser.getSelectedFiles();

                    keepAdding.setTrue();
                    stop.setEnabled( true );
                    /* a nice shade of red */
                    stop.setBackground( new Color( 219, 79, 87 ) );
                    addFile.setEnabled( false );

                    final Lambda0 continue_ = new Lambda0(){
                        public Object invoke(){
                            return Boolean.valueOf( keepAdding.getValue() );
                        }
                    };

                    final Lambda2 retain = new Lambda2(){
                        public Object invoke( final Object node, final Object children ){
                            SwingUtilities.invokeLater( new Runnable(){
                                public void run(){
                                    fileTree.insert( (TreeNode) node, (int[]) children );
                                    files.expandRow( 0 );
                                }
                            });
                            return null;
                        }
                    };

                    new Thread(){
                        public void run(){
                            TreePath path = files.getSelectionPath();
                            FileTreeNode node = null;
                            if ( path == null ){
                                node = (FileTreeNode) fileTree.getRoot();
                            } else {
                                node = (FileTreeNode) path.getLastPathComponent();
                            }
                            synchronized( fileTree.getRoot() ){
                                Reft.addFiles( node, myfiles, continue_, retain, 0 );
                            }
                            // addFiles( fileTree, myfiles, continue_, retain, 0 );
                            // retain.invoke_();
                            // saveObject( files, persistFilesName );

                            SwingUtilities.invokeLater( new Runnable(){
                                public void run(){
                                    // fileTree.update();
                                    stop.setEnabled( false );
                                    stop.setBackground( null );
                                    addFile.setEnabled( true );
                                }
                            } );
                        }
                    }.start();
                }
            }
        });

        final Lambda0 doDelete = new Lambda0(){
            public Object invoke(){
                /* stop adding files and delete the selected paths
                */
                keepAdding.setFalse();
                synchronized( fileTree.getRoot() ){
                    TreePath[] paths = files.getSelectionPaths();
                    fileTree.removeAll( paths );
                }

                return null;
            }
        };

        deleteFile.addActionListener( new AbstractAction(){
            public void actionPerformed( ActionEvent event ){
                doDelete.invoke_();
            }
        });

        final MutableString serverName = new MutableString( "localhost" );

        final Lambda0 setURL = new Lambda0(){
            public Object invoke(){
                TreePath path = files.getSelectionPath();
                if ( path != null ){
                    FileTreeNode node = (FileTreeNode) path.getLastPathComponent();
                    if ( node.isDirectory() ){
                        url.setText( "http://" + serverNameField.getText() + ":" + httpPortField.getText() + node.getLineage() + "/" );
                    } else {
                        StringBuffer buffer = new StringBuffer();
                        buffer.append( "http://" );
                        buffer.append( serverNameField.getText() );
                        buffer.append( ":" );
                        buffer.append( httpPortField.getText() );
                        buffer.append( ((FileTreeNode) node.getParent()).getLineage() );
                        buffer.append( "/file?id=" );
                        buffer.append( node.getParent().getIndex( node ) );
                        url.setText( buffer.toString() );
                    }
                }
                return null;
            }
        };

        files.addTreeSelectionListener( new TreeSelectionListener(){
            public void valueChanged( TreeSelectionEvent event ){
                TreePath path = event.getPath();
                if ( path != null ){
                    FileTreeNode node = (FileTreeNode) path.getLastPathComponent();
                    addFile.setEnabled( node.isDirectory() );
                    setURL.invoke_();
                } else {
                    addFile.setEnabled( true );
                }
            }
        });

        files.addKeyListener( new KeyAdapter(){
            public void keyTyped( KeyEvent e ){
                if ( e.getKeyChar() == KeyEvent.VK_DELETE ){
                    doDelete.invoke_();
                }
            }
        });

        httpPortField.addActionListener( new AbstractAction(){
            public void actionPerformed( ActionEvent e ){
                try{
                    int i = Integer.parseInt( httpPortField.getText() );
                    serverIsOn.setFalse();
                    off.setSelected( true );
                    restartHTTPServer( fileTree, i, log );
                    serverIsOn.setTrue();
                    setURL.invoke_();
                    setBrowse.invoke_();
                    on.setSelected( true );

                    log.invoke_( LogInfo, "Started HTTP server on port " + i );
                } catch ( NumberFormatException ne ){
                    System.out.println( "Couldn't restart HTTP server on port '" + httpPortField.getText() + "'" );
                    log.invoke_( LogImportant, "Could not start HTTP server on port " + httpPortField.getText() );
                } catch ( IOException ie ){
                    log.invoke_( LogImportant, "Could not start HTTP server on port " + httpPortField.getText() );
                }
            }
        });

        serverNameField.addActionListener( new AbstractAction(){
            public void actionPerformed( ActionEvent event ){
                serverName.set( serverNameField.getText() );
                setURL.invoke_();
                setBrowse.invoke_();
            }
        });

        setBrowse.invoke_();

        // startHTTPServer( files, 4050 );
    }

    private synchronized void restartHTTPServer( final FileTree files, int port, final Lambda2 log ) throws IOException {
        stopHTTPServer();
        startHTTPServer( files, port, log );
    }

    private synchronized void stopHTTPServer(){
        if ( httpserver != null ){
            httpserver.shutdown();
        }
    }

    /* start an http server whose sole job in life is to
     * list files to be downloaded by people browsing
     * the site.
     */
    private synchronized void startHTTPServer( final FileTree fileTree, int port, final Lambda2 log ) throws IOException {
        Properties properties = new Properties();
        properties.setProperty( "port", String.valueOf( port ) );
        properties.setProperty( "logger.level", "debug" );
        httpserver = new net.sf.ooweb.Server( new Configuration( properties ) );

        /*
           FileTreeNode root = (FileTreeNode) files.getRoot();
           synchronized( root ){
           registerNode( httpserver, root, "/", files );
           }
           */

        httpserver.setDynamicRegister( new Lambda1(){
            public Object invoke( Object o ){
                String[] paths = (String[]) o;
                FileTreeNode node = fileTree.findNode( paths );

                if ( node != null ){
                    return createNodeHandler( fileTree, node, log );
                }

                return null;
            }
        });

        httpserver.startUp();

        /* the http server */
        new Thread(){
            public void run(){
                httpserver.listen();
            }
        }.start();
    }

    private Object createNodeHandler( final FileTree fileTree, final FileTreeNode node, final Lambda2 log ){
        return new Object(){

            private double reduce( double num ){
                return ((int)( num * 100 )) / 100.0;
            }

            private String niceSize( long size ){
                if ( size < 1024 ){
                    return String.valueOf( size );
                } else if ( size < 1024 * 1024 ){
                    return String.valueOf( reduce( size / 1024.0 ) ) + "k";	
                } else if ( size < 1024 * 1024 * 1024 ){
                    return String.valueOf( reduce( size / ( 1024.0 * 1024.0 ) ) ) + "m";
                } else {
                    return String.valueOf( reduce( size / ( 1024.0 * 1024.0 * 1024.0 ) ) ) + "g";
                }
            }

            public String index(){
                synchronized( fileTree.getRoot() ){
                    log.invoke_( LogDebug, Thread.currentThread().getName() + " requested index for /" + node.getLineage() );
                    List files = node.getFiles();
                    StringBuffer result = new StringBuffer();
                    int num = 0;
                    String lineage = node.getLineage();

                    result.append( "<html><body>" );

                    result.append( "Current directory: " + lineage + "<br />" );
                    result.append( "<a href='..'>Go up one directory</a>" );
                    result.append( "<br /><br />" );
                    result.append( "<table>" );
                    result.append( "<tr><td>Name</td><td>Type</td><td>Size</td></tr>" );
                    for ( Iterator iterator = files.iterator(); iterator.hasNext(); ){
                        FileTreeNode f = (FileTreeNode) iterator.next();
                        System.out.println( "Get node for " + f.getName() );
                        result.append( "<tr>" );
                        if ( f.isDirectory() ){
                            result.append( "<td>" );
                            result.append( "<a href='"  + lineage + "/" + f.getName() + "/'>" + f.getName() + "/</a>" );
                            result.append( "</td><td>Directory</td><td>-</td>" );
                        } else {
                            result.append( "<td>" );
                            result.append( "<a href='" + lineage + "/file?id=" + num + "'>" + f.getName() + "</a>" );
                            result.append( "</td><td>File</td>" );
                            result.append( "<td>" + niceSize( f.getFile().length() ) + "</td>" );
                        }
                        result.append( "</tr>" );
                        num++;
                    }
                    result.append( "</table>" );

                    result.append( "</body></html>" );

                    return result.toString();
                }
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
                synchronized( fileTree.getRoot() ){
                    try{
                        int i = Integer.parseInt( id );
                        List files = node.getFiles();
                        FileTreeNode f = (FileTreeNode) files.get( i );
                        log.invoke_( LogDebug, Thread.currentThread().getName() + " requested file " + f.getLineage() );
                        // HTTP.redirect( f.getName() );
                        return f;
                        // return f.getUserObject();
                    } catch ( Exception e ){
                        e.printStackTrace();
                    }
                    return null;
                }
            }
        };
    }
}

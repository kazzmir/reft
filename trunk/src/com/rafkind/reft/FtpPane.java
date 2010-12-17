package com.rafkind.reft;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.Component;

import org.swixml.SwingEngine;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JRadioButton;
import javax.swing.JTree;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.tree.TreeNode;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.awt.Color;

import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreePath;

import javax.swing.SwingUtilities;

import rheise.jftpd.FTPException;

import java.util.Properties;

import java.io.File;
import java.io.IOException;

public class FtpPane {

	private static final Integer LogDebug = new Integer(3);
	private static final Integer LogInfo = new Integer(2);
	private static final Integer LogImportant = new Integer(1);
	private static final Integer LogNone = new Integer(0);

	private final static int DEFAULT_PORT = 4051;
	
	private rheise.jftpd.Server ftpserver;
	private JPanel panel;

	private String logToDescription(int i){
		if (i == LogDebug.intValue()){
			return "Log everything";
		}
		if (i == LogInfo.intValue()){
			return "Log most things";
		}
		if (i == LogImportant.intValue()){
			return "Log important things";
		}
		if (i == LogNone.intValue()){
			return "Log nothing";
		}
		return "Log";
	}

	public JPanel getPanel(){
		return panel;
	}

	private SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private String currentTime(){
		return date.format(Calendar.getInstance().getTime());
	}

	public FtpPane(){
		SwingEngine engine = new SwingEngine("ftp.xml");
		panel = (JPanel) engine.getRootComponent();

		final JTextField serverNameField = (JTextField) engine.find("server");
		final JTextField portField = (JTextField) engine.find("port");

		final StringBuffer messages = new StringBuffer();
		final JTextArea logs = (JTextArea) engine.find("log");
		// final JList fileList = (JList) engine.find("fileList");
		
		final JLabel logDescription = (JLabel) engine.find("log-level-desc");
		final JSlider logLevel = (JSlider) engine.find("log-level");

		final JTree files = (JTree) engine.find("fileList");
		final FileTree fileTree = new FileTree();
		files.setModel(fileTree);

		// fileList.setListData(files);

		files.addKeyListener(new KeyAdapter(){
			public void keyTyped(KeyEvent e){
				if (e.getKeyChar() == KeyEvent.VK_DELETE){
					// System.out.println("Delete something");
					TreePath[] selected = files.getSelectionPaths();
					if (selected != null){
						fileTree.remove(selected);
					}
				}
			}
		});
		
		final MutableLong currentLogLevel = new MutableLong(LogDebug.intValue());

		logLevel.setValue((int) currentLogLevel.getValue());
		logDescription.setText(logToDescription(logLevel.getValue()));
		logLevel.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e){
				currentLogLevel.setValue(logLevel.getValue());
				logDescription.setText(logToDescription(logLevel.getValue()));
			}
		});

		final JRadioButton on = (JRadioButton) engine.find("on");
		final JRadioButton off = (JRadioButton) engine.find("off");
		off.setSelected(true);

		final MutableBoolean serverIsOn = new MutableBoolean(false);

		final Lambda1 okLogLevel = new Lambda1(){
			public Object invoke(Object level){
				int i = ((Integer) level).intValue();
				return Boolean.valueOf(currentLogLevel.getValue() >= i);
			}
		};

		final Lambda2 log = new Lambda2(){
			public Object invoke(Object level, Object message){
				if (Boolean.TRUE.equals(okLogLevel.invoke_(level))){
					messages.append(currentTime()).append(" ");
					messages.append((String) message).append("\n");
					logs.setText(messages.toString());
				}
				return null;

			}
		};

		/* invoked when the RETR ftp command is issued */
		final Lambda1 logFile = new Lambda1(){
			public Object invoke(Object m) throws Exception {
				return log.invoke(LogDebug, m);
			}
		};

		on.addActionListener(new AbstractAction(){
			public void actionPerformed(ActionEvent ae){
				if (serverIsOn.isFalse()){
					try{
						int i = Integer.parseInt(portField.getText());
						serverIsOn.setFalse();
						off.setSelected(true);
						startFTPServer(fileTree, i, logFile);
						serverIsOn.setTrue();
						on.setSelected(true);

						log.invoke_(LogInfo, "Started FTP server on port " + portField.getText());
					} catch (NumberFormatException ne){
						System.out.println("Couldn't restart FTP server on port '" + portField.getText() + "'");
						log.invoke_(LogImportant, "Could not start FTP server on port " + portField.getText());
					} catch (FTPException e){
						e.printStackTrace();
						log.invoke_(LogImportant, "Could not start FTP server on port " + portField.getText());
					}
				}
			}
		});

		/* turn off the ftp server */
		off.addActionListener(new AbstractAction(){
			public void actionPerformed(ActionEvent ae){
				if (serverIsOn.isTrue()){
					stopFTPServer();
					serverIsOn.setFalse();
					log.invoke_(LogInfo, "FTP Server is off");
				}
			}
		});
		
		final JTextField browse = (JTextField) engine.find("browse");

		final Lambda0 setBrowse = new Lambda0(){
			public Object invoke(){
				browse.setText("ftp://" + serverNameField.getText() + ":" + portField.getText() + "/");
				return null;
			}
		};

		portField.setText(String.valueOf(DEFAULT_PORT));
		serverNameField.setText("localhost");
		final JTextField url = (JTextField) engine.find("url");

		final MutableBoolean keepAdding = new MutableBoolean();

		final JButton addFile = (JButton) engine.find("add");
		final JButton addDir = (JButton) engine.find("add_dir");
		final JButton deleteFile = (JButton) engine.find("delete");
		final JButton stop = (JButton) engine.find("stop");

		addFile.setToolTipText("<html>This button will bring up a new window that lets you select files to share over FTP.<br>Be sure to turn the server on to actually share them.</html>");

		addDir.setToolTipText("Add a new sub-folder. This is useful for organizing many files into a single logical place.");

		deleteFile.setToolTipText("Remove all selected files from the list above.");
		stop.setToolTipText("When many files are being added this button lets you stop the addition process");

		stop.addActionListener(new AbstractAction(){
			public void actionPerformed(ActionEvent event){
				keepAdding.setFalse();
			}
		});

		addDir.addActionListener(new AbstractAction(){
			private FileTreeNode addFolder(FileTreeNode addTo){
				FileTreeNode node = new FileTreeNode("type directory here", true);
				addTo.add(node);
				int[] children = new int[ 1 ];
				children[ 0 ] = addTo.getChildCount() - 1;
				fileTree.insert(addTo, children);
				return node;
			}

			public void actionPerformed(ActionEvent event){
				final TreePath path = files.getSelectionPath();
				if (path != null){
					FileTreeNode node = (FileTreeNode) path.getLastPathComponent();
					if (node.isDirectory()){
						FileTreeNode dir = addFolder((FileTreeNode) path.getLastPathComponent());
						TreePath update = path.pathByAddingChild(dir);
						files.scrollPathToVisible(update);
						files.startEditingAtPath(update);
					}
				}
			}
		});

		addFile.addActionListener(new AbstractAction(){
			public void actionPerformed(ActionEvent event){
				JFileChooser chooser = new JFileChooser(new File("."));
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.setMultiSelectionEnabled(true);
				int returnVal = chooser.showOpenDialog(getPanel());
				if (returnVal == JFileChooser.APPROVE_OPTION){
					final File[] myfiles = chooser.getSelectedFiles();

					keepAdding.setTrue();
					stop.setEnabled(true);
					/* a nice shade of red */
					stop.setBackground(new Color(219, 79, 87));
					addFile.setEnabled(false);

					final Lambda0 continue_ = new Lambda0(){
						public Object invoke(){
							return Boolean.valueOf(keepAdding.getValue());
						}
					};

					final Lambda2 retain = new Lambda2(){
						public Object invoke(final Object node, final Object children){
							SwingUtilities.invokeLater(new Runnable(){
								public void run(){
									fileTree.insert((TreeNode) node, (int[]) children);
									files.expandRow(0);
								}
							});
							return null;
						}
					};

					new Thread(){
						public void run(){
							TreePath path = files.getSelectionPath();
							FileTreeNode node = null;
							if (path == null){
								node = (FileTreeNode) fileTree.getRoot();
							} else {
								node = (FileTreeNode) path.getLastPathComponent();
							}
							synchronized(fileTree.getRoot()){
								Reft.addFiles(node, myfiles, continue_, retain, 0);
							}
							// addFiles(fileTree, myfiles, continue_, retain, 0);
							// retain.invoke_();

							SwingUtilities.invokeLater(new Runnable(){
								public void run(){
									// fileTree.update();
									stop.setEnabled(false);
									stop.setBackground(null);
									addFile.setEnabled(true);
								}
							});
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
				synchronized(fileTree.getRoot()){
					TreePath[] paths = files.getSelectionPaths();
					fileTree.removeAll(paths);
				}

				return null;
			}
		};

		deleteFile.addActionListener(new AbstractAction(){
			public void actionPerformed(ActionEvent event){
				doDelete.invoke_();
			}
		});

		final MutableString serverName = new MutableString("localhost");

		final Lambda0 setURL = new Lambda0(){
			public Object invoke(){
				TreePath path = files.getSelectionPath();
				if (path != null){
					FileTreeNode node = (FileTreeNode) path.getLastPathComponent();
					if (node.isDirectory()){
						url.setText("ftp://" + serverNameField.getText() + ":" + portField.getText() + node.getLineage() + "/");
					} else {
						StringBuffer buffer = new StringBuffer();
						buffer.append("ftp://");
						buffer.append(serverNameField.getText());
						buffer.append(":");
						buffer.append(portField.getText());
						buffer.append(((FileTreeNode) node.getParent()).getLineage());
						buffer.append("/");
						buffer.append(node.getName());
						url.setText(buffer.toString());
					}
				}
				return null;
			}
		};

		files.addTreeSelectionListener(new TreeSelectionListener(){
			public void valueChanged(TreeSelectionEvent event){
				TreePath path = event.getPath();
				if (path != null){
					FileTreeNode node = (FileTreeNode) path.getLastPathComponent();
					addFile.setEnabled(node.isDirectory());
					setURL.invoke_();
				} else {
					addFile.setEnabled(true);
				}
			}
		});

		files.addKeyListener(new KeyAdapter(){
			public void keyTyped(KeyEvent e){
				if (e.getKeyChar() == KeyEvent.VK_DELETE){
					doDelete.invoke_();
				}
			}
		});

		portField.addActionListener(new AbstractAction(){
			public void actionPerformed(ActionEvent e){
				try{
					int i = Integer.parseInt(portField.getText());
					serverIsOn.setFalse();
					off.setSelected(true);
					restartFTPServer(fileTree, i, logFile);
					serverIsOn.setTrue();
					setURL.invoke_();
					setBrowse.invoke_();
					on.setSelected(true);

					log.invoke_(LogInfo, "Started FTP server on port " + i);
				} catch (NumberFormatException ne){
					System.out.println("Couldn't restart FTP server on port '" + portField.getText() + "'");
					log.invoke_(LogImportant, "Could not start FTP server on port " + portField.getText());
				} catch (FTPException ie){
					log.invoke_(LogImportant, "Could not start FTP server on port " + portField.getText());
				}
			}
		});

		serverNameField.addActionListener(new AbstractAction(){
			public void actionPerformed(ActionEvent event){
				serverName.set(serverNameField.getText());
				setURL.invoke_();
				setBrowse.invoke_();
			}
		});

		setBrowse.invoke_();
	}

	private synchronized void startFTPServer(final FileTree manager, int port, final Lambda1 log) throws FTPException {
		Properties properties = new Properties();
		ftpserver = new rheise.jftpd.Server(properties, port);
		new Thread(){
			public void run(){
				ftpserver.start(manager, log);
			}
		}.start();
	}

	private void restartFTPServer(FileTree manager, int port, Lambda1 log) throws FTPException {
		stopFTPServer();
		startFTPServer(manager, port, log);
	}

	private synchronized void stopFTPServer(){
		if (ftpserver != null){
			ftpserver.stop();
		}
	}
}

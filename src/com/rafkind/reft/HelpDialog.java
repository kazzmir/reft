package com.rafkind.reft;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;

public class HelpDialog extends JDialog {

	public HelpDialog(){

		setTitle( "Help" );

		setSize( 400, 300 );
		final HelpDialog help = this;

		JComponent all = Box.createVerticalBox();
		this.getContentPane().add( all );

		JTextArea text = new JTextArea( getHelpMessage(), 20, 20 );
		text.setLineWrap( true );
		text.setWrapStyleWord( true );
		text.setEditable( false );
		JScrollPane scroll = new JScrollPane( text );
		all.add( scroll );

		final Lambda0 close = new Lambda0(){
			public Object invoke(){
				help.dispose();
				return null;
			}
		};
		
		all.add( Box.createVerticalStrut( 10 ) );
		JButton closeButton = new JButton( "Close" );
		all.add( closeButton );
		closeButton.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent action ){
				try{
					close.invoke();
				} catch ( Exception ex ){
				}
			}
		});

		this.addWindowListener( new CloseHook( close ) );
	}

	private String getHelpMessage(){
		StringBuffer buffer = new StringBuffer();
		buffer.append( "Reft - Ridiculously Easy File Transfer" ).append( "\n" );
		buffer.append( "\n" );
		buffer.append( "Reft transfers files between two computers without any hassle. Add a computer using the buddy list by entering their name, ip address, and port and then you can send them a file. If someone else sends you a file you will see a popup asking if you want to accept that file. If you accept, the file transfer will begin and thats all there is to it." ).append( "\n\n" );
		buffer.append( "On the REFT pane you can add a buddy with the 'Add' button. Type in the users nickname, their ip address/dns name and the port( defaults to 4949 ). After you add the buddy you can select him/her and then click 'Send File'. On the send file dialog box you can add a bunch of files and directories to send. Once you are done selecting files click 'Send Files' and the files will be sent to your buddy who is running REFT also. They will see a dialog box pop up on their screen asking if its ok to receive these files and if they choose 'Ok' then the transfer will begin( and hopefully end ). Your buddies are stored to disk so they will show up again if you exit REFT and reload the program." ).append( "\n\n" );
		buffer.append( "On the HTTP pane you can share files over HTTP and your friends can download the files using their webbrowser. Add some files with the 'Add' button. The HTTP server starts out as off so these files are not shared until you click 'on'. Using your web browser you can see what files are shared if you goto http://localhost:4050, assuming you havent changed the port of the HTTP server. If you click on a file in the list in the HTTP pane the URL text field will change showing you what the URL is to download this file. Give this URL to your friend when you want them to download a specific file. You can give the browse text field to a friend when you want them to see all the files you are sharing. Change the 'server' text field to automatically update the URL and Browse fields so that its easy to copy/paste them to your friends. On/Off turn the HTTP on or off and the port field changes what port the HTTP server is running on." ).append( "\n\n" );

		buffer.append( "\n" );
		buffer.append( "Made by Jon Rafkind" ).append( "\n" );
		buffer.append( "jon@rafkind.com" );

		return buffer.toString();

	}

}

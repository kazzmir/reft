package com.rafkind.reft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;

public class Transfer{

	private InputStream input;
	private OutputStream output;
	private JComponent box;
	// private JButton cancel;

	private boolean done = false;
	private long size;
	private MutableBoolean canceled;

	private Lambda1 loopLambda;

	public Transfer( InputStream input, OutputStream output, Lambda1 addComponent, String filename, final long size, String host, final Lambda1 updateLambda, MutableBoolean canceled ){
		this.input = input;
		this.output = output;
		final Transfer transfer = this;

		this.size = size;

		final JProgressBar bar = new JProgressBar( 0, 100 );

		this.canceled = canceled;

		JComponent top = new JPanel();
		// top.setAlignmentX( JComponent.LEFT_ALIGNMENT );
		top.setBorder( BorderFactory.createEtchedBorder() );
		box = Box.createVerticalBox();
		top.add( box );
		box.add( new JLabel( "File: " + filename ) );
		box.add( Box.createVerticalStrut( 2 ) );
		box.add( new JLabel( "Size: " + size ) );
		// box.add( Box.createVerticalStrut( 2 ) );
		// box.add( new JLabel( "IP: " + host ) );
		box.add( Box.createVerticalStrut( 2 ) );
		final JLabel transferred = new JLabel( "Transferred: 0" );
		box.add( transferred );
		box.add( Box.createVerticalStrut( 2 ) );
		box.add( bar );
		box.add( Box.createVerticalStrut( 2 ) );

		/*
		cancel = new JButton( "Cancel" );
		box.add( cancel );

		cancel.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent action ){
				transfer.stop();
				box.remove( cancel );
				box.revalidate();
			}
		});
		*/

		final MutableLong totalSize = new MutableLong();
			
		loopLambda = new Lambda1(){
			public Object invoke( Object arg ){
				try{
					updateLambda.invoke( arg );
				} catch ( Exception ex ){
				}

				totalSize.add( ((Long) arg).longValue() );
				// long i = ((Long) arg).longValue();
				bar.setValue( (int)(totalSize.getValue() * 100 / size) );
				transferred.setText( "Transferred: " + totalSize.getValue() );
				return null;
			}
		};
		
		try{
			addComponent.invoke( top );
		} catch ( Exception ex ){
		}
	}

	private synchronized void stop(){
		done = true;
	}

	private synchronized boolean isDone(){
		return done || canceled.isTrue();
	}

	public boolean transfer() throws IOException {
		byte[] buf = new byte[ 1 << 16 ];

		int max = size > buf.length ? buf.length : (int) size;
		int num = input.read( buf, 0, max );
		long total = 0;

		// System.out.println( Thread.currentThread().getName() + ": read " + num );

		while ( total < size && num != -1 && ! isDone() ){
			total += num;
			loopLambda.invoke_( new Long( num ) );
			// bar.setValue( (int)(total * 100 / max) );
			output.write( buf, 0, num );
			max = (int)(size - total) > buf.length ? buf.length : (int)(size - total);
			// System.out.println( Thread.currentThread().getName() + ": Size = " + size + " Total = " + total + " Max buf = " + max );
			num = input.read( buf, 0, max );
			// System.out.println( Thread.currentThread().getName() + ": read " + num );
		}

		// System.out.println( Thread.currentThread().getName() + ": Size = " + size + " total sent = " + total );

		if ( total != this.size ){
			SwingUtilities.invokeLater( new Runnable(){
				public void run(){
					box.add( new JLabel( "Transfer failed!" ) );
					box.revalidate();
				}
			});
		}

		SwingUtilities.invokeLater( new Runnable(){
			public void run(){
				// box.remove( cancel );
				box.revalidate();
			}
		});

		return total == this.size;
	}
}

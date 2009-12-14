package com.rafkind.reft;

import java.awt.Graphics;
import javax.swing.border.BevelBorder;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class LoadingWindow extends JFrame {

	private Object lock = null;
	private Lambda1 updater;
	public LoadingWindow( Object lock ){
		this.lock = lock;
		JPanel panel = new JPanel();
		this.getContentPane().add( panel );
		panel.add( new JLabel( "Loading Reft" ) );
		panel.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
		final JProgressBar bar = new JProgressBar( 0, 100 );

		updater = new Lambda1(){
			public Object invoke( Object i ){
				bar.setValue( ((Integer) i).intValue() );
				return null;
			}
		};
		
		panel.add( bar );
		
		this.setResizable( false );
		this.setSize( 200, 50 );
		this.setUndecorated( true );
	}

	public void update( int i ){
		updater.invoke_( new Integer( i ) );
	}

	public void paint( Graphics g ){
		super.paint( g );
		if ( lock != null ){
			synchronized( lock ){
				lock.notify();
			}
			lock = null;
		}
	}
}

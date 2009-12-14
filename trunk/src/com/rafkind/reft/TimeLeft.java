package com.rafkind.reft;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JLabel;

public class TimeLeft extends JLabel{

	private final long max;

	public TimeLeft( long max ){
		super( "Time left: forever" );
		this.max = max;
	}

	public void update( long value, double speed ){
		/* basically forever */
		if ( speed < 0.000001 ){
			this.setText( "Time left: forever" );
		} else {
			this.setText( "Time left: " + decimals(((max - value) / speed), 2) + " seconds" );
		}
	}

	private double decimals( double num, int places ){
		return (int)( num * Math.pow( 10, places ) ) / Math.pow( 10, places );
	}
}

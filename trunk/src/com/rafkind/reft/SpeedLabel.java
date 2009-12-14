package com.rafkind.reft;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JLabel;

public class SpeedLabel extends JLabel{

	private class Unit{
		
		public Unit( long data ){
			this.data = data;
			this.time = System.currentTimeMillis();
		}

		public long data;
		public long time;
	}

	private List units;

	/* only accept the Nth update */
	private int accept;

	public SpeedLabel(){
		super( "Speed:" );
		units = new ArrayList();
	}

	public void update( long data ){
		accept++;
		if ( accept > 0 ){
			accept = 0;
			units.add( new Unit( data ) );
			if ( units.size() > 10 ){
				units.remove( 0 );
			}

			this.setText( "Speed: " + calculateSpeed() );
		}
	}

	private double decimals( double num, int places ){
		return (int)( num * Math.pow( 10, places ) ) / Math.pow( 10, places );
	}

	public double rawSpeed(){
		if ( units.isEmpty() || units.size() == 1 ){
			return 0;
		}

		double data = 0;
		double time = 0;
		Iterator iterator = units.iterator();
		Unit last = (Unit) iterator.next();
		for ( ; iterator.hasNext(); ){
			Unit next = (Unit) iterator.next();
			data += next.data - last.data;
			time += next.time - last.time;
			last = next;
		}

		// System.out.println( "Data = " + data + " Time = " + time );

		return data * 1000 / time;
	}

	private String calculateSpeed(){
		
		double all = rawSpeed();
		/*
		Unit begin = (Unit) units.get( 0 );
		Unit end = (Unit) units.get( units.size() - 1 );

		all = (double) (end.data - begin.data) / (end.time - begin.time);
		*/

		if ( all < 1024 ){
			return decimals( all, 2 ) + " bytes/s";
		} else if ( all < 1024 * 1024 ){
			return decimals( all / 1024, 2 ) + " kb/s";
		} else if ( all < 1024 * 1024 * 1024 ){
			return decimals( all / (1024 * 1024), 2 ) + " mb/s";
		} else {
			return decimals( all / (1024 * 1024 * 1024), 2 ) + " gb/s";
		}
	}

}

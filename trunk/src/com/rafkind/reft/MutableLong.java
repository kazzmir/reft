package com.rafkind.reft;

public class MutableLong{
	private long value;

	public MutableLong(){
		this( 0 );
	}

	public MutableLong( long value ){
		this.value = value;
	}

	public void add( long value ){
		this.value += value;
	}

	public void setValue( long value ){
		this.value = value;
	}

	public long getValue(){
		return this.value;
	}
}

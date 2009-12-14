package com.rafkind.reft;

public class MutableString{

	private String value;

	public MutableString(){
		this( "" );
	}
	
	public MutableString( String value ){
		this.set( value );
	}

	public void set( String value ){
		this.value = value;
	}

	public String get(){
		return value;
	}
}

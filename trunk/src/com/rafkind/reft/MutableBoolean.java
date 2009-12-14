package com.rafkind.reft;

public class MutableBoolean{

	private boolean value;

	public MutableBoolean(){
		this( false );
	}
	
	public MutableBoolean( boolean value ){
		this.value = value;
	}

	public synchronized boolean getValue(){
		return value;
	}

	public synchronized void setTrue(){
		this.value = true;
	}

	public synchronized void setFalse(){
		this.value = false;
	}

	public synchronized boolean isTrue(){
		return this.value == true;
	}

	public synchronized boolean isFalse(){
		return this.value == false;
	}
}

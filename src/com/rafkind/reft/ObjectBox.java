package com.rafkind.reft;

public class ObjectBox{
	public ObjectBox(){
		o = null;
	}

	public synchronized void set( Object o ){
		this.o = o;
	}

	public synchronized Object get(){
		return o;
	}

	public synchronized boolean isEmpty(){
		return o == null;
	}

	private Object o;
}

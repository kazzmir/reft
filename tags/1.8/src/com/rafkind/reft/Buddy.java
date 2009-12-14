package com.rafkind.reft;

import java.io.Serializable;

public class Buddy implements Serializable {

	private String name;
	private String ip;
	private int port;

	public Buddy( String name, String ip, int port ){
		this.name = name;
		this.ip = ip;
		this.port = port;
	}

	public String getIP(){
		return this.ip;
	}

	public void setIP( String ip ){
		this.ip = ip;
	}

	public int getPort(){
		return this.port;
	}

	public void setPort( int p ){
		this.port = p;
	}

	public String getName(){
		return this.name;
	}

	public void setName( String s ){
		this.name = s;
	}

	public String toString(){
		return getName();
	}
}

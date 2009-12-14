package com.rafkind.reft.stream;

public class Mark{

	private byte[] data;
	private int length;
	private long id;

	public Mark( byte[] data, int length, long id ){
		this.data = data;
		this.length = length;
		this.id = id;
	}

	public byte[] getData(){
		return data;
	}

	public int getLength(){
		return length;
	}

	public long getId(){
		return id;
	}

}

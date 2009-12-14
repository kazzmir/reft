package com.rafkind.reft;

import java.net.ServerSocket;
import java.net.Socket;

import java.io.IOException;

public class ServerThread extends Thread {

	private Lambda1 lambda;
	private boolean quit;
	private boolean running = false;
	private ServerSocket listen;

	public ServerThread( int port, Lambda1 lambda ) throws IOException {
		this.lambda = lambda;
		this.quit = false;

		listen = new ServerSocket( port );
	}

	private synchronized boolean shouldQuit(){
		return this.quit;
	}

	public synchronized void kill(){
		this.quit = true;
		try{
			listen.close();
		} catch ( IOException ie ){
			ie.printStackTrace();
		}
	}

	public void setPort( int port ) throws IOException {
		listen.close();
		listen = new ServerSocket( port );
		quit = false;
	}

	public synchronized boolean isRunning(){
		return running;
	}

	public void run(){

		running = true;

		while ( ! shouldQuit() ){

			try{
				Socket client = listen.accept();
				lambda.invoke( client );
			} catch ( IOException ie ){
				System.out.println( "Listen socket closed" );
				ie.printStackTrace();
			} catch ( Exception e ){
				e.printStackTrace();
			}

			try{
				Thread.sleep( 1 );
			} catch ( Exception e ){
			}

		}

		running = false;
	}
}

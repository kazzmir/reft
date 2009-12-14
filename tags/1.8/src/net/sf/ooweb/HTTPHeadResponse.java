package net.sf.ooweb;

import java.io.PrintStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;

import net.sf.ooweb.util.Logger;

class HTTPHeadResponse extends HTTPResponse {

	HTTPHeadResponse(String applicationName, PrintStream ps, Logger logger) {
		super(applicationName, ps, logger);
	}
	
	protected String contentLength( File target ){
		return null;
	}
		
	protected void writeStream( InputStream stream ) throws IOException {
	}
}

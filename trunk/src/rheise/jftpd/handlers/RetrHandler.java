package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda1;
import com.rafkind.reft.Lambda2;

import rheise.jftpd.CommandHandler;
import rheise.jftpd.CommandException;

import java.util.StringTokenizer;

public class RetrHandler implements CommandHandler {

	private Lambda1 sendFile;
	private Lambda2 reply;

	public RetrHandler( Lambda2 reply, Lambda1 sendFile ){
		this.sendFile = sendFile;
		this.reply = reply;
	}

	public void handle( String line, StringTokenizer st ) throws CommandException {
		String path = null;
		try{
			path = line.substring(5);
		} catch (Exception e){
			throw new CommandException( 550, "Pathname invalid" );
		}

		try{
			sendFile.invoke( path );
		} catch ( CommandException ce ){
			throw ce;
		} catch ( Exception e ){
			throw new CommandException( 550, "Could not send file", e );
		}
	}
}

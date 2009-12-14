package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda1;

import rheise.jftpd.CommandHandler;
import rheise.jftpd.CommandException;

import java.util.StringTokenizer;

public class UnhandledHandler implements CommandHandler {

	public UnhandledHandler(){
	}

	public void handle( String line, StringTokenizer tokenizer ) throws CommandException {
		throw new CommandException(500, "'" + line + "': command not supported.");
	}
}

package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda1;
import com.rafkind.reft.Lambda2;

import rheise.jftpd.CommandHandler;

public class HelpHandler extends UnhandledHandler {

	private Lambda2 reply;

	public HelpHandler( Lambda2 reply ){
		this.reply = reply;
	}
}
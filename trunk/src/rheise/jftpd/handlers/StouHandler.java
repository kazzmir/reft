package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda1;
import com.rafkind.reft.Lambda2;

import rheise.jftpd.CommandHandler;

public class StouHandler extends UnhandledHandler {

	private Lambda2 reply;

	public StouHandler( Lambda2 reply ){
		this.reply = reply;
	}
}

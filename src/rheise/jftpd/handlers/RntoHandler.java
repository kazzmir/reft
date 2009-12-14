package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda1;
import com.rafkind.reft.Lambda2;

import rheise.jftpd.CommandHandler;

public class RntoHandler extends UnhandledHandler {

	private Lambda2 reply;

	public RntoHandler( Lambda2 reply ){
		this.reply = reply;
	}
}

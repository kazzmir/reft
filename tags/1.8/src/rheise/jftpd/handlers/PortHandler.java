package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda0;
import com.rafkind.reft.Lambda1;
import com.rafkind.reft.Lambda2;

import java.net.Socket;

import rheise.jftpd.CommandHandler;
import rheise.jftpd.CommandException;

import java.util.StringTokenizer;

public class PortHandler implements CommandHandler {

	private Lambda1 setDataPort;
	private Lambda2 reply;

	public PortHandler( Lambda2 reply, Lambda1 setDataPort ){
		this.setDataPort = setDataPort;
		this.reply = reply;
	}

	public void handle( String line, StringTokenizer st ) throws CommandException {
		try{
			String portStr = st.nextToken();
			st = new StringTokenizer(portStr, ",");
			String h1 = st.nextToken();
			String h2 = st.nextToken();
			String h3 = st.nextToken();
			String h4 = st.nextToken();
			int p1 = Integer.parseInt(st.nextToken());
			int p2 = Integer.parseInt(st.nextToken());

			final String dataHost = h1 + "." + h2 + "." + h3 + "." + h4;
			final int dataPort = (p1 << 8) | p2;

			Lambda0 get = new Lambda0(){
				public Object invoke() throws Exception {
					return new Socket( dataHost, dataPort );
				}
			};

			setDataPort.invoke( get );
			reply.invoke( new Integer( 200 ), "PORT command successful." );
		} catch ( Exception e ){
			throw new CommandException( 500, "Could not open PORT", e );
		}
	}
}

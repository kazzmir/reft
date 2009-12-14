package rheise.jftpd;

public class KillableThread extends Thread {

	public KillableThread( Runnable r ){
		super( r );
	}

	public void kill(){
		throw new NullPointerException();
	}

}

package rheise.jftpd;

import java.util.StringTokenizer;

public interface CommandHandler{
	public void handle( String line, StringTokenizer tokenizer ) throws CommandException;
}

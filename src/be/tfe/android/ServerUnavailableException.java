package be.tfe.android;

public class ServerUnavailableException extends Exception {
	private static final long serialVersionUID = 1L;

	ServerUnavailableException()
	{
		super();
	}
	
	ServerUnavailableException(String msg)
	{
		super(msg);
	}
}

package be.tfe.android.misc.utils;

public class ServerUnavailableException extends Exception {
	private static final long serialVersionUID = 1L;

	public ServerUnavailableException()
	{
		super();
	}
	
	public ServerUnavailableException(String msg)
	{
		super(msg);
	}
}

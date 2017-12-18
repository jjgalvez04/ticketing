package com.galvez.demos.ticketing.exceptions;

public class TicketUnavailableException extends TicketException {

	private static final long serialVersionUID = 5468367944499647547L;

	public TicketUnavailableException(String message) {
		super(message);
	}

}

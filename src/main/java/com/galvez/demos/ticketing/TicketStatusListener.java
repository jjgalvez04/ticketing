package com.galvez.demos.ticketing;

public interface TicketStatusListener {

	/**
	 * Notifies the listener that the current ticket has changed status
	 * 
	 * @param ticket
	 */
	public void notifyStatusChange(Ticket ticket);

}

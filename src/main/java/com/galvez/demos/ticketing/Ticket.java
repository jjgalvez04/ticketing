package com.galvez.demos.ticketing;

import com.galvez.demos.ticketing.exceptions.TicketUnavailableException;

public interface Ticket {

	/**
	 * @return
	 */
	public double getTicketPrice();

	/**
	 * @return
	 */
	public int getSeatNumber();

	/**
	 * @return
	 */
	public String getSeatRow();

	/**
	 * @return
	 */
	public TicketStatus getStatus();

	/**
	 * @throws TicketUnavailableException
	 */
	public void reserveTicket() throws TicketUnavailableException;

	/**
	 * @throws TicketUnavailableException
	 */
	public void purchaseTicket() throws TicketUnavailableException;

	/**
	 * @throws TicketUnavailableException
	 */
	public void releaseTicket() throws TicketUnavailableException;

}

package com.galvez.demos.ticketing;

import java.util.List;

import com.galvez.demos.ticketing.exceptions.TicketUnavailableException;

/**
 * The SeatHold contains the information for tickets reserved
 * 
 * @author jgalve
 *
 */
public interface SeatHold {

	/**
	 * Confirms the seats for the specified customer email.
	 * 
	 * @param customerEmail
	 *            Customer Email to confirm the ownership of this hold
	 * @return A confirmation code for the transaction
	 * @throws TicketUnavailableException
	 *             if the tickets are no longer held by this customer
	 */
	public String confirmSeats(String customerEmail) throws TicketUnavailableException;

	/**
	 * Returns the tickets in this hold
	 * 
	 * @return List<Ticket> with the tickets in this hold
	 */
	public List<Ticket> getTickets();

	/**
	 * Returns the total price for the tickets in this hold
	 * 
	 * @return double with the total ticket price
	 */
	public double getTotalPrice();

	/**
	 * Returns the unique identifier for this seat hold (Note: a String would be a
	 * better unique option here, something to enhance in the future)
	 * 
	 * @return id with the unique identifier for this hold
	 */
	public int getSeatHoldId();

	/**
	 * Notifies a listener when a ticket changes from reserved back to available
	 * 
	 * @param statusListener
	 */
	public void notifyTicketAvailableAgain(TicketStatusListener statusListener);
}

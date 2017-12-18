package com.galvez.demos.ticketing.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.galvez.demos.ticketing.SeatHold;
import com.galvez.demos.ticketing.Ticket;
import com.galvez.demos.ticketing.TicketStatus;
import com.galvez.demos.ticketing.TicketStatusListener;
import com.galvez.demos.ticketing.exceptions.TicketUnavailableException;

/**
 * This SeatHold implementation contains the information for tickets reserved
 * and has functionality to either release the tickets after a specified time or
 * to make the purchase complete
 * 
 * @author jgalve
 *
 */
public class SeatHoldImpl implements SeatHold {

	private static final String TICKET_RELEASE_ERROR = "Ticket row %s number %s is in status %s and cannot be released";
	// 5 seconds to purchase (for testing)
	private static final long RELEASE_TIMEOUT = 5000L;
	// Tickets in this seat hold
	private List<Ticket> tickets;
	// Total price for all the tickets
	private double totalPrice;
	// Unique identifier for the seat hold
	private int seatHoldId;
	// Customer holding the tickets
	private String customerEmail;
	// Timer to expire the reservation and return the tickets to available
	private Timer expirationTimer;
	private List<TicketStatusListener> ticketListeners;

	/**
	 * Creates a new SeatHold with the specified tickets and attached to the
	 * provided customerEmail. It automatically generates a new ticket ID and starts
	 * a timer to automatically release the tickets if not purchased before the
	 * expiration time.
	 * 
	 * @param tickets
	 *            List of tickets for this hold
	 * @param customerEmail
	 *            Customer email to be attached to this hold
	 * @throws TicketUnavailableException
	 *             if the tickets specified are not available to reserve
	 */
	public SeatHoldImpl(List<Ticket> tickets, String customerEmail) throws TicketUnavailableException {
		this.tickets = tickets;
		for (Ticket ticket : tickets) {
			totalPrice += ticket.getTicketPrice();
			ticket.reserveTicket();
		}

		seatHoldId = new Random().nextInt(10000);
		this.customerEmail = customerEmail;
		expirationTimer = new Timer();
		expirationTimer.schedule(new ReleaseTicketsTask(), RELEASE_TIMEOUT);
	}

	/**
	 * Timer to release the tickets
	 * 
	 * @author jgalve
	 *
	 */
	class ReleaseTicketsTask extends TimerTask {
		@Override
		public void run() {
			releaseTickets();
			expirationTimer.cancel();
		}
	}

	public String confirmSeats(String customerEmail) throws TicketUnavailableException {
		if (!this.customerEmail.equals(customerEmail)) {
			return null;
		}

		expirationTimer.cancel();

		for (Ticket ticket : tickets) {
			switch (ticket.getStatus()) {
			case AVAILABLE:
				throw new TicketUnavailableException("Reservation has expired");
			case SOLD:
				throw new TicketUnavailableException("The specified tickets are no longer available");
			case RESERVED:
				ticket.purchaseTicket();
			}
		}
		return UUID.randomUUID().toString();
	}

	public List<Ticket> getTickets() {
		return tickets;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	// Releases the tickets in this hold
	private void releaseTickets() {
		for (Ticket ticket : tickets) {
			try {
				ticket.releaseTicket();
				for (TicketStatusListener listener : ticketListeners) {
					listener.notifyStatusChange(ticket);
				}
			} catch (TicketUnavailableException e) {
				// If ticket is already available we don't care about it. If it was already sold
				// we are in trouble so throw a Runtime exception
				if (ticket.getStatus() != TicketStatus.AVAILABLE) {
					throw new RuntimeException(String.format(TICKET_RELEASE_ERROR, ticket.getSeatRow(),
							ticket.getSeatNumber(), ticket.getStatus().toString()));
				}
			}
		}
	}

	public int getSeatHoldId() {
		return seatHoldId;
	}

	public void notifyTicketAvailableAgain(TicketStatusListener listener) {
		if (ticketListeners == null) {
			ticketListeners = new ArrayList<TicketStatusListener>();
		}
		ticketListeners.add(listener);
	}

}

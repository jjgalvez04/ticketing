package com.galvez.demos.ticketing;

import com.galvez.demos.ticketing.exceptions.TicketException;
import com.galvez.demos.ticketing.exceptions.TicketUnavailableException;

public interface TicketService {
	/**
	 * The number of seats in the venue that are neither held nor reserved
	 *
	 * @return the number of tickets available in the venue
	 */
	int numSeatsAvailable();

	/**
	 * Find and hold the best available seats for a customer
	 *
	 * @param numSeats
	 *            the number of seats to find and hold
	 * @param customerEmail
	 *            unique identifier for the customer
	 * @return a SeatHold object identifying the specific seats and related
	 *         information
	 * @throws TicketUnavailableException
	 *             if there are not enough tickets available for the request
	 */
	SeatHold findAndHoldSeats(int numSeats, String customerEmail) throws TicketUnavailableException;

	/**
	 * Commit seats held for a specific customer
	 *
	 * @param seatHoldId
	 *            the seat hold identifier
	 * @param customerEmail
	 *            the email address of the customer to which the seat hold is
	 *            assigned
	 * @return a reservation confirmation code
	 * @throws TicketException
	 *             if there was a problem with the reservation
	 */
	String reserveSeats(int seatHoldId, String customerEmail) throws TicketException;
}

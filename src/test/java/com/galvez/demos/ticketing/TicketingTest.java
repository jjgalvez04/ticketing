package com.galvez.demos.ticketing;

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.galvez.demos.ticketing.exceptions.TicketException;
import com.galvez.demos.ticketing.exceptions.TicketUnavailableException;
import com.galvez.demos.ticketing.impl.EventTicketService;

/**
 * Unit test for Ticketing Demo App.
 */
public class TicketingTest {

	private Event movieEvent;
	private Event theaterEvent;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public TicketingTest() {
		String[] rows = { "A", "B", "C", "D", "E", "F", "G" };
		Calendar calendar = Calendar.getInstance();
		calendar.set(2018, 1, 3, 18, 00);
		movieEvent = new EventTicketService("Movie", calendar.getTime(), EventType.SCREEN, rows, 12, 20.0);
		theaterEvent = new EventTicketService("Theater", calendar.getTime(), EventType.STAGE, rows, 12, 20.0);
	}

	@Test
	public void testReserveRowAB() throws TicketUnavailableException, InterruptedException {
		Assert.assertEquals(84, theaterEvent.numSeatsAvailable());
		SeatHold hold = theaterEvent.findAndHoldSeats(7, "myemail@company.com");
		for (Ticket ticket : hold.getTickets()) {
			Assert.assertEquals("A", ticket.getSeatRow());
		}
		Assert.assertEquals(77, theaterEvent.numSeatsAvailable());
		SeatHold hold2 = theaterEvent.findAndHoldSeats(6, "myemail@company.com");
		for (Ticket ticket : hold2.getTickets()) {
			Assert.assertEquals("B", ticket.getSeatRow());
		}
		Assert.assertEquals(71, theaterEvent.numSeatsAvailable());
		// Let them expire
		Thread.sleep(10000);
		Assert.assertEquals(84, theaterEvent.numSeatsAvailable());
	}

	@Test
	public void testReserveRowGF() throws TicketException {
		Assert.assertEquals(84, movieEvent.numSeatsAvailable());
		// In a movie event the best tickets are on the back, not the front
		SeatHold hold = movieEvent.findAndHoldSeats(8, "myemail@company.com");
		Assert.assertEquals("G", hold.getTickets().get(0).getSeatRow());
		Assert.assertEquals(76, movieEvent.numSeatsAvailable());
		// Then it should be filling from the back down
		hold = movieEvent.findAndHoldSeats(8, "myemail@company.com");
		Assert.assertEquals("F", hold.getTickets().get(0).getSeatRow());
		Assert.assertEquals(68, movieEvent.numSeatsAvailable());
	}

	@Test
	public void testReserveBest() throws TicketUnavailableException {
		SeatHold hold = theaterEvent.findAndHoldSeats(5, "myemail@company.com");
		// Seat numbers are between 1 and 12, the best 5 would be between seats 5 and 9
		for (Ticket ticket : hold.getTickets()) {
			Assert.assertEquals("A", ticket.getSeatRow());
			Assert.assertTrue(ticket.getSeatNumber() >= 5 && ticket.getSeatNumber() <= 9);
		}
		// Running it again should give the same tickets in row B
		hold = theaterEvent.findAndHoldSeats(5, "myemail@company.com");
		for (Ticket ticket : hold.getTickets()) {
			Assert.assertEquals("B", ticket.getSeatRow());
			Assert.assertTrue(ticket.getSeatNumber() >= 5 && ticket.getSeatNumber() <= 9);
		}
	}

	@Test
	public void testFullReserve() throws TicketException, InterruptedException {
		Assert.assertEquals(84, theaterEvent.numSeatsAvailable());
		SeatHold hold = theaterEvent.findAndHoldSeats(5, "myemail@company.com");
		theaterEvent.reserveSeats(hold.getSeatHoldId(), "myemail@company.com");
		Assert.assertEquals(79, theaterEvent.numSeatsAvailable());
		Thread.sleep(8000);
		// Even after expiring, these tickets shouldn't be available anymore
		Assert.assertEquals(79, theaterEvent.numSeatsAvailable());
		// Getting new tickets should send us to row B if they don't fit in Row A
		hold = theaterEvent.findAndHoldSeats(9, "myemail@company.com");
		Assert.assertEquals("B", hold.getTickets().get(0).getSeatRow());
		// But if they fit they should be in Row A
		hold = theaterEvent.findAndHoldSeats(2, "myemail@company.com");
		Assert.assertEquals("A", hold.getTickets().get(0).getSeatRow());
	}

	@Test
	public void testExpiration() throws TicketException, InterruptedException {
		Assert.assertEquals(84, theaterEvent.numSeatsAvailable());
		SeatHold hold = theaterEvent.findAndHoldSeats(5, "myemail@company.com");
		Assert.assertEquals(79, theaterEvent.numSeatsAvailable());
		Thread.sleep(8000);
	
		// Try to reserve after the time expired
		Assert.assertEquals(84, theaterEvent.numSeatsAvailable());
		thrown.expect(TicketUnavailableException.class);
		thrown.expectMessage("Reservation has expired");
		theaterEvent.reserveSeats(hold.getSeatHoldId(), "myemail@company.com");
	}

	@Test
	public void testDoubleReserve() throws TicketException, InterruptedException {
		Assert.assertEquals(84, theaterEvent.numSeatsAvailable());
		SeatHold hold = theaterEvent.findAndHoldSeats(5, "myemail@company.com");
		theaterEvent.reserveSeats(hold.getSeatHoldId(), "myemail@company.com");
		Assert.assertEquals(79, theaterEvent.numSeatsAvailable());
	
		// Try to reserve again should fail
		thrown.expect(TicketUnavailableException.class);
		thrown.expectMessage("The specified tickets are no longer available");
		theaterEvent.reserveSeats(hold.getSeatHoldId(), "myemail@company.com");
	}

	@Test
	public void testEmailValidation() throws TicketException {
		SeatHold hold = theaterEvent.findAndHoldSeats(7, "myemail@company.com");

		// This should fail, the registered email address is wrong
		thrown.expect(TicketException.class);
		thrown.expectMessage("The confirmation code and email do not match");
		theaterEvent.reserveSeats(hold.getSeatHoldId(), "anotherEmail@company.com");
	}
}

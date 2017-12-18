# ticketing-demo
## Overview
This application simulates a ticketing system in which a customer may review the number of tickets available for an event, reserve tickets and purchase them.
Once tickets are reserved, the user will have a certain amount of time to complete the transaction before the tickets are released again. In order to make the tests run faster, the current time out is 5 seconds, but this can be adjusted for real life scenarios.
After user is ready, the tickets can be purchased and a confirmation number will be provided.

## Classes
In order to make this project work, the following interfaces were created:
- TicketService - returns the number of seats available, allows for reservations and purchases
- Event - Event extends from TicketService but adds Event related information, like event name, date, event type and tickets
- SeatHold - Container for the reserved tickets, it allows the reserved tickets to be purchased
- Ticket - Ticket object
- TicketStatusListener - Listener for ticket status changes

The following Enumerations are also needed:
- EventType - An event can be Screen based or Stage based. The difference is that for Stage based events the tickets are better the closer they are to the stage, while for Screen based the tickets are better the further away from the screen.
- TicketStatus - A ticket can be either Available, Reserved or Sold

The following concrete classes were also created:
- EventTicketService - This class implements both TicketService and Event. Creates an event with a seating map and has all the functionality for reserving and purchasing tickets
- SeatHoldImpl - This class implements the SeatHold interface. It also manages the timeout for reservations to be released and for purchases of tickets already reserved
- TicketRow - This is a row of seats inside an event. It is capable of finding the best tickets within the row
- TicketImpl - Implementation of the Ticket interface
- ConfirmedTickets - Once tickets are confirmed, this object contains a confirmation code, an email address and the tickets purchased.

## Assumptions and limitations
In this first demo, it is not possible to select different tickets than the ones provided by the system. At the same time, if a user wants to buy more than one row of tickets it would not be possible in one single transaction.

It is assumed that the best tickets are always in the middle of the row and then moving towards the sides before moving on to the next rows. For Stage based events, the best tickets are closer to the stage while for Screen based events the further to the screen, the better the tickets.

## Instructions for executing
The project was created to be run and tested in maven. The simplest way to compile and run the tests is to clone the repository and then run "mvn package" inside the location.


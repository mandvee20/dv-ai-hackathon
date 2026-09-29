package com.fias.simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class FiasSimulator
{

   private static final int PORT = 9000;

   private static final int MIN_ROOM = 100;

   private static final int MAX_ROOM = 999;

   private static final int EVENT_INTERVAL_SECONDS = 10;

   private final Queue<FiasEvent> pendingEvents = new ConcurrentLinkedQueue<>();

   private final Random random = new Random();

   private final AtomicBoolean running = new AtomicBoolean(true);

   public static void main(String[] args)
   {
      new FiasSimulator().start();
   }

   private void start()
   {

      printBanner();

      startEventGenerator();

      startConsoleListener();

      startTcpServer();
   }

   /**
    * Starts the TCP server.
    *
    * PMSI connects to this server and fetches pending FIAS room-status events.
    */
   private void startTcpServer()
   {

      try (ServerSocket serverSocket = new ServerSocket(PORT))
      {

         System.out.println("FIAS TCP server started on port " + PORT);

         while (running.get())
         {

            Socket socket = serverSocket.accept();

            System.out.println("\n[PMSI CONNECTED] " + socket.getRemoteSocketAddress());

            Thread.ofVirtual().name("fias-client").start(() -> handleClient(socket));
         }

      }
      catch (IOException e)
      {

         if (running.get())
         {

            System.err.println("FIAS server error: " + e.getMessage());
         }
      }
   }

   /**
    * Generates random FIAS room-status events at a fixed interval.
    */
   private void startEventGenerator()
   {

      Thread.ofVirtual().name("fias-event-generator").start(() -> {

         while (running.get())
         {

            generateRandomEvent();

            try
            {

               Thread.sleep(EVENT_INTERVAL_SECONDS * 1000L);

            }
            catch (InterruptedException e)
            {

               Thread.currentThread().interrupt();

               return;
            }
         }
      });
   }

   /**
    * Generates one random room-status event and stores it as a pending event.
    */
   private void generateRandomEvent()
   {

      String roomNumber = String.valueOf(MIN_ROOM + random.nextInt(MAX_ROOM - MIN_ROOM + 1));

      int roomStatus = 1 + random.nextInt(6);

      FiasEvent event = new FiasEvent(roomNumber, roomStatus);

      pendingEvents.add(event);

      System.out.println(
               "[EVENT GENERATED] " + LocalDateTime.now() + " | " + event.toMessage() + " | " + event.statusDescription());
   }

   /**
    * Sends pending FIAS events to PMSI.
    */
   private void handleClient(Socket socket)
   {

      try (socket; PrintWriter writer = new PrintWriter(socket.getOutputStream(), true))
      {

         int sentEvents = 0;

         FiasEvent event;

         while ((event = pendingEvents.poll()) != null)
         {

            String message = event.toMessage();

            writer.println(message);

            sentEvents++;

            System.out.println("[EVENT SENT] " + message + " | " + event.statusDescription());
         }

         if (sentEvents == 0)
         {

            System.out.println("[PMSI FETCH] " + "No pending events.");

         }
         else
         {

            System.out.println("[PMSI FETCH] Sent " + sentEvents + " event(s).");
         }

      }
      catch (IOException e)
      {

         System.err.println("[PMSI CONNECTION ERROR] " + e.getMessage());
      }
   }

   /**
    * Reads commands from the simulator console.
    */
   private void startConsoleListener()
   {

      Thread.ofVirtual().name("fias-console").start(() -> {

         try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)))
         {

            while (running.get())
            {

               String command = reader.readLine();

               if (command == null)
               {
                  return;
               }

               switch (command.trim().toLowerCase())
               {

                  case "g", "generate" -> generateRandomEvent();

                  case "p", "pending" ->
                           System.out.println("[PENDING EVENTS] " + pendingEvents.size());

                  case "q", "quit", "exit" ->
                  {

                     running.set(false);

                     System.out.println("Stopping FIAS simulator...");

                     return;
                  }

                  default -> System.out.println(
                           "Commands: " + "g=generate, " + "p=pending, " + "q=quit");
               }
            }

         }
         catch (IOException e)
         {

            if (running.get())
            {

               System.err.println("Console error: " + e.getMessage());
            }
         }
      });
   }

   private void printBanner()
   {

      System.out.println();
      System.out.println("======================================");

      System.out.println("          FIAS PMS SIMULATOR");

      System.out.println("======================================");

      System.out.println("TCP Port       : " + PORT);

      System.out.println("Event interval : " + EVENT_INTERVAL_SECONDS + " seconds");

      System.out.println("Room range     : " + MIN_ROOM + "-" + MAX_ROOM);

      System.out.println();

      System.out.println("RS values:");

      System.out.println("1 = Dirty/Vacant");

      System.out.println("2 = Dirty/Occupied");

      System.out.println("3 = Clean/Vacant");

      System.out.println("4 = Clean/Occupied");

      System.out.println("5 = Inspected/Vacant");

      System.out.println("6 = Inspected/Occupied");

      System.out.println();

      System.out.println("Console commands:");

      System.out.println("g = generate event");

      System.out.println("p = pending event count");

      System.out.println("q = quit");

      System.out.println("======================================");

      System.out.println();
   }
}

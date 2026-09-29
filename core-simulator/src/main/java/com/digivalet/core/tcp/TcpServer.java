package com.digivalet.core.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TcpServer
{
   private static final Logger log =
         LoggerFactory.getLogger(TcpServer.class);

   private static final int TCP_PORT = 9000;

   private final TcpClientHandler tcpClientHandler;

   public TcpServer(TcpClientHandler tcpClientHandler)
   {
      this.tcpClientHandler = tcpClientHandler;
   }

   @PostConstruct
   public void start()
   {
      Thread.startVirtualThread(() -> {

         try (ServerSocket serverSocket =
                    new ServerSocket(TCP_PORT))
         {
            log.info("TCP server started on port {}", TCP_PORT);

            while (true)
            {
               Socket socket = serverSocket.accept();

               log.info("TCP client connected from {}",
                     socket.getRemoteSocketAddress());

               Thread.startVirtualThread(
                     () -> tcpClientHandler.handle(socket));
            }
         }
         catch (IOException e)
         {
            log.error("TCP server failed", e);
         }

      });
   }
}

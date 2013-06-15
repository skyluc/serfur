package org.skyluc.serfur.sample

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel

import scala.concurrent.Future
import scala.concurrent.Promise

import org.skyluc.serfur.CompletionHandlerForPromise
import org.skyluc.serfur.ConnectionHandler
import org.skyluc.serfur.Server

object EchoServer {

  def main(args: Array[String]) {
    Server(new ConnectionHandler() {
      def doIt(buffer: ByteBuffer, socket: AsynchronousSocketChannel): Future[Integer] = {

        // setup the buffer to be written out
        buffer.flip()

        val writtingPromise = Promise[Integer]

        socket.write(buffer, writtingPromise, new CompletionHandlerForPromise[Integer])

        writtingPromise.future
      }
    })

    readLine
  }

}
package org.skyluc.serfur

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Promise

object Server {

  val NoFuture = Future.successful[Unit]()

  def apply(handler: ConnectionHandler): Server = {

    val serverSocket = AsynchronousServerSocketChannel.open().bind(null)

    val server = new Server(serverSocket, handler)

    server.acceptConnections()

    println(s"Server started on $serverSocket")

    server
  }

}

class Server private (serverSocket: AsynchronousServerSocketChannel, handler: ConnectionHandler) {

  private def acceptConnections() {
    val connectionAcceptedPromise = Promise[AsynchronousSocketChannel]()

    serverSocket.accept(connectionAcceptedPromise, new CompletionHandlerForPromise[AsynchronousSocketChannel])

    connectionAcceptedPromise.future.foreach {
      socket =>
        IncomingConnection(socket, handler)
        acceptConnections()
    }
  }

}

object IncomingConnection {

  def apply(socket: AsynchronousSocketChannel, handler: ConnectionHandler): IncomingConnection = {
    val conn = new IncomingConnection(socket, handler)
    // Initiating the processing incoming bytes
    conn.process()

    conn
  }
}

class IncomingConnection(socket: AsynchronousSocketChannel, handler: ConnectionHandler) {

  def process() {

    val buffer = ByteBuffer.allocate(1024)

    val promise = Promise[Integer]

    socket.read(buffer, promise, new CompletionHandlerForPromise[Integer])

    promise.future flatMap {
      byteRead =>
        if (byteRead < 0) {
          // time to close
          socket.close()
          Server.NoFuture
        } else {
          handler.doIt(buffer, socket).map {
            Unit =>
              process
          }
        }
    }
  }
}

trait ConnectionHandler {

  /**
   * The action to take when bytes are received
   */
  def doIt(buffer: ByteBuffer, socket: AsynchronousSocketChannel): Future[_ >: Unit]

}

class CompletionHandlerForPromise[T] extends CompletionHandler[T, Promise[T]] {
  def completed(result: T, promise: Promise[T]): Unit = {
    promise.success(result)
  }
  def failed(e: Throwable, promise: Promise[T]): Unit = {
    promise.failure(e) // need to think about this ...
  }
}


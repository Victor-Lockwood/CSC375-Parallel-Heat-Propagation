package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class ServerMain {
    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {

        //Referred to here:
        // https://gist.github.com/chatton/14110d2550126b12c0254501dde73616
        int port = Integer.parseInt(args[0]);
        ServerSocket ss = new ServerSocket(port);

        System.out.println("Accepting connection on port " + port);
        ForkJoinPool fjp = ForkJoinPool.commonPool();

        while(true) {
            Socket socket = ss.accept();
            System.out.println("Got connection from " + socket);

            //Some assistance from here: https://stackoverflow.com/questions/27736175/how-to-send-receive-objects-using-sockets-in-java
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            NetworkObject networkObject = (NetworkObject) objectInputStream.readObject();


            ServerWorker worker = new ServerWorker(networkObject);

            fjp.invoke(worker);

            fjp.awaitQuiescence(2, TimeUnit.SECONDS);

            objectOutputStream.writeObject(worker.writeGrid);

        }

    }
}

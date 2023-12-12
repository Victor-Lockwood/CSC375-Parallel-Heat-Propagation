import Server.ServerGrid;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;

//Some assistance from here: https://stackoverflow.com/questions/27736175/how-to-send-receive-objects-using-sockets-in-java
public class ClientWorker implements Runnable {

    public Server.NetworkObject networkObject;

    public ServerGrid returnedGrid;

    public int portNumber;

    public String hostName;

    public CountDownLatch countDownLatch;

    public ClientWorker(Server.NetworkObject networkObject, int portNumber, String hostName) {
        this.networkObject = networkObject;
        this.portNumber = portNumber;
        this.hostName = hostName;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(hostName, portNumber);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            objectOutputStream.writeObject(networkObject);

            returnedGrid = (ServerGrid) objectInputStream.readObject();

            socket.close();
            countDownLatch.countDown();
        } catch (ClassNotFoundException | IOException ignored) {
            System.out.println("Exception.");
            //I know I know I know I know
        }
        return;
    }
}

import Server.ServerGrid;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//Some assistance from here: https://stackoverflow.com/questions/27736175/how-to-send-receive-objects-using-sockets-in-java
public class ClientWorker implements Runnable {

    public Server.NetworkObject networkObject;

    public ServerGrid returnedGrid;

    public int portNumber;

    public String hostName;

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

        } catch (ClassNotFoundException | IOException ignored) {
            //I know I know I know I know
        }
        return;
    }
}

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PrimeSlaveServer {
    public static void main(String[] args) {
        try {
            Socket socket = connectToMaster("25.11.247.20", 1235);
            runSlave(socket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Socket connectToMaster(String ipAddress, int port) throws IOException {
        System.out.println("Connecting to the Master Server...");
        return new Socket(ipAddress, port);
    }

    private static void runSlave(Socket socket) {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("Slave Server Connected to the Master Server");

            while (true) {
                try {
                    PrimeChecker task = (PrimeChecker) in.readObject();
                    System.out.println("Received task: " + task.lowerBound + " " + task.upperBound);

                    long startTime = System.nanoTime();
                    task.execute(8, out);
                    System.out.println("Slave Server Completion Time: " + (System.nanoTime() - startTime) / 1e9);

                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

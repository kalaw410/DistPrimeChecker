import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class PrimeMasterServer {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            ServerSocket slaveServerSocket = new ServerSocket(1235);
            Socket slaveSocket = slaveServerSocket.accept();

            System.out.println("Slave Connected");
            ObjectOutputStream slaveOutputStream = new ObjectOutputStream(slaveSocket.getOutputStream());
            ObjectInputStream slaveInputStream = new ObjectInputStream(slaveSocket.getInputStream());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client Connected");
                ObjectInputStream clientInputStream = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream clientOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());

                PrimeChecker clientTask = (PrimeChecker) clientInputStream.readObject();
                System.out.println(clientTask.lowerBound + " " + clientTask.upperBound);

                PrimeChecker slaveTask = new PrimeChecker((clientTask.lowerBound + clientTask.upperBound) / 4 + 1, (clientTask.lowerBound + clientTask.upperBound) * 3 / 4);
                sendTaskToSlave(slaveTask, slaveOutputStream);

                long startTime = System.nanoTime();
                PrimeChecker masterTask1 = new PrimeChecker(clientTask.lowerBound, (clientTask.lowerBound + clientTask.upperBound) / 4);
                PrimeChecker masterTask2 = new PrimeChecker((clientTask.lowerBound + clientTask.upperBound) * 3 / 4 + 1, clientTask.upperBound);

                executeMasterTasks(8, masterTask1, masterTask2, clientOutputStream);
                System.out.println("Master Server Completion Time: " + (System.nanoTime() - startTime) / 1e9);

                processSlaveResults(slaveInputStream, clientOutputStream);

                clientSocket.close();
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void sendTaskToSlave(PrimeChecker task, ObjectOutputStream slaveOutputStream) throws IOException {
        slaveOutputStream.writeObject(task);
    }

    private static void executeMasterTasks(int numThreads, PrimeChecker task1, PrimeChecker task2, ObjectOutputStream clientOutputStream)
            throws IOException, InterruptedException {
        task1.execute(numThreads, clientOutputStream);
        task2.execute(numThreads, clientOutputStream);
    }

    private static void processSlaveResults(ObjectInputStream slaveInputStream, ObjectOutputStream clientOutputStream)
            throws IOException, ClassNotFoundException {
        while (true) {
            Object slaveResult = slaveInputStream.readObject();
            if (slaveResult.equals("end")) {
                clientOutputStream.writeObject("Finished");
                break;
            }
            ArrayList<Integer> primes = (ArrayList<Integer>) slaveResult;
            clientOutputStream.writeObject(primes);
        }
    }
}


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class PrimeClient {
    public static void main(String[] args) {
        try {
            Socket socket = connectToMaster("localhost", 1234);
            System.out.println("Connected to Server");

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            ArrayList<Integer> allPrimes = executeTaskAndGetPrimes(in, out);
            System.out.println("Total Primes: " + allPrimes.size());

            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Socket connectToMaster(String ipAddress, int port) throws IOException {
        return new Socket(ipAddress, port);
    }

    private static ArrayList<Integer> executeTaskAndGetPrimes(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
        Scanner sc = new Scanner(System.in);
        int start = sc.nextInt();
        int end = sc.nextInt();

        PrimeChecker task = new PrimeChecker(start, end);
        System.out.println("Sending task to Master Server");
        out.writeObject(task);

        ArrayList<Integer> allPrimes = new ArrayList<>();

        while (true) {
            Object batchFromMaster = in.readObject();

            if (batchFromMaster.equals("Finished")) {
                break;
            } else if (batchFromMaster.equals("end")) {
                continue;
            }
            ArrayList<Integer> primes = (ArrayList<Integer>) batchFromMaster;
            allPrimes.addAll(primes);
        }

        return allPrimes;
    }
}






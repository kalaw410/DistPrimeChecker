import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;

public class PrimeChecker implements Serializable {
    public int lowerBound;
    public int upperBound;

    PrimeChecker(int lowerBound, int upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public void execute(int numThreads, ObjectOutputStream outputStream) throws IOException, InterruptedException {
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            int finalI = i;
            threads[finalI] = new Thread(() -> runThread(numThreads, finalI, outputStream));
            threads[finalI].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        outputStream.writeObject("end");
    }

    private void runThread(int numThreads, int threadIndex, ObjectOutputStream outputStream) {
        Stack<Integer> primes = new Stack<>();
        for (int num = lowerBound + threadIndex; num <= upperBound; num += numThreads) {
            if (isPrime(num)) {
                primes.push(num);
            }
            if (primes.size() >= 100) {
                sendBatch(primes, outputStream);
            }
        }
        if (!primes.isEmpty()) {
            sendBatch(primes, outputStream);
        }
    }

    private void sendBatch(Stack<Integer> primes, ObjectOutputStream outputStream) {
        ArrayList<Integer> batch = new ArrayList<>();
        while (!primes.isEmpty()) {
            batch.add(primes.pop());
        }
        synchronized (outputStream) {
            try {
                outputStream.writeObject(batch);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        }
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }
}

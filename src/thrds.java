import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Scanner;
import java.util.Random;
import java.util.Arrays;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

class mythread extends Thread {
    int number;
    mythread(int number){
        this.number = number;
    }


}

class quickTh extends Thread {
    static List<mythread> threads = new ArrayList<mythread>();
    static int threads_left;
    static int threads_overall;
    static int num_of_finished;
    static int[] priorities;
    static ReentrantLock mutex = new ReentrantLock();



    public static void multithreadsort(int[] A, int lo, int hi){
        if (lo < hi) {
            int p = partition(A, lo, hi);
            if (threads_left > 1) {
                threads_left -= 2;
                mythread newth = new mythread(threads_left+2) {
                    public void run() {
                        mutex.lock();
                        try {
                            System.out.println(String.format("thread %d started, sorting boundaries:%d - %d", this.number, lo, p-1));
                            this.setPriority(priorities[this.number]);
                            multithreadsort(A, lo, p - 1);
                        } finally {
                            System.out.println(String.format("thread %d finished", this.number));
                            num_of_finished += 1;
//                            System.out.println(num_of_finished + threads_overall);
                            mutex.unlock();
                        }
                        while (!Thread.currentThread().isInterrupted()) {
                            try {
                                this.sleep(20);
                            } catch (InterruptedException e) {
//                                e.printStackTrace();
                                break;
                            }
                        }
                    }
                };
                newth.start();
                threads.add(newth);

                newth = new mythread(threads_left+1) {
                    public void run() {
                        mutex.lock();
                        try {
                            System.out.println(String.format("thread %d started, sorting boundaries:%d - %d", this.number, p+1, hi));
                            this.setPriority(priorities[this.number]);
                            multithreadsort(A, p + 1, hi);
                        } finally {
                            System.out.println(String.format("thread %d finished", this.number));
                        num_of_finished += 1;
//                            System.out.println(num_of_finished + threads_overall);
                            mutex.unlock();
                        }
                        while (!Thread.currentThread().isInterrupted()) {
                            try {
                                this.sleep(20);
                            } catch (InterruptedException e) {
//                                    e.printStackTrace();
                                break;
                            }
                        }
                    }

                };
//                threads_left--;
//                try {
//                    newth.join();
//                } catch (Exception ex){
//                    System.out.println("exception: " + ex);
//                }
                newth.start();
                threads.add(newth);
            }
            if (threads_left == 1) {
                mythread newth = new mythread(threads_left) {
                    public void run() {
                        mutex.lock();
                        try {
                            System.out.println(String.format("thread %d started, sorting boundaries:%d - %d", this.number, lo, p-1));
                            this.setPriority(priorities[this.number]);
                            multithreadsort(A, lo, p - 1);
                        } finally {
                            System.out.println(String.format("thread %d finished", this.number));
                            num_of_finished += 1;
//                            System.out.println(num_of_finished + threads_overall);
                            mutex.unlock();
                        }
                        while (!Thread.currentThread().isInterrupted()) {
                            try {
                                this.sleep(20);
                            } catch (InterruptedException e) {
//                                e.printStackTrace();
                                break;
                            }
                        }
                    }
                };
                newth.start();
                threads.add(newth);
                quicksort(A, p + 1, hi);

            }

            else {
                quicksort(A, lo, p - 1);
                quicksort(A, p + 1, hi);
            }
        }
    }

    static int partition(int[] A, int lo, int hi){
        int pivot = A[hi];
        int i = lo;
            for(int j =lo; j<hi; j++) {
                if (A[j] > pivot) {
                    int tmp = A[i];
                    A[i] = A[j];
                    A[j] = tmp;
                    i++;
                }
            }
        int tmp = A[i];
        A[i] = A[hi];
        A[hi] = tmp;
        return i;
    }
    static int[] quicksort(int[] A, int lo, int hi){
        if (lo < hi){
            int p = partition(A, lo, hi);
            quicksort(A, lo, p - 1);
            quicksort(A, p + 1, hi);
        }
        return A;
    }

    static RandomAccessFile writeToMMF(RandomAccessFile file, String string_to_write) throws IOException {

            FileChannel fileChannel = file.getChannel();
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, string_to_write.length());

            buffer.put(string_to_write.getBytes());
            return file;
    }

    static void readFromMMF(RandomAccessFile file) throws IOException {
        //Get file channel in read-only mode
        FileChannel fileChannel = file.getChannel();

        //Get direct byte buffer access using channel.map() operation
        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());

        for (int i = 0; i < buffer.limit(); i++)
        {
            System.out.print((char) buffer.get()); //Print the content of file
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner input = new Scanner(System.in);
        Random rand = new Random();
        System.out.println("number of threads:");
        threads_left = input.nextInt();
//        threads_left = 4;
        threads_overall = threads_left;
        num_of_finished = 0;
        System.out.println("lenght of an array:");
        int n = input.nextInt();
//        int n = 1000000;
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = (int) rand.nextInt(1000);
        }
        File file = new File("ramfilearray.dat");

        //Delete the file; we will create a new file
        file.delete();
        RandomAccessFile ramFile = new RandomAccessFile(file, "rw");
        writeToMMF(ramFile, Arrays.toString(a));
        priorities = new int[threads_overall + 1];
        Arrays.fill(priorities, 5);
        System.out.println(String.format("choose a thread to set priority: (%d - %d); 0 to continue", 1, threads_overall));
        int num_of_thread = input.nextInt();
        while (num_of_thread != 0) {
            System.out.println("set priority from 1 to 10, where 1 is MIN_PRIORITY");
            int priority_of_thread = input.nextInt();
            priorities[num_of_thread] = priority_of_thread;
            System.out.println(String.format("choose a thread to set priority: (%d - %d); 0 to continue", 1, threads_overall));
            num_of_thread = input.nextInt();
        }

        System.out.println("sorting...");
        multithreadsort(a, 0, a.length - 1);
        while (num_of_finished < threads_overall) {
//            System.out.println("stuck");
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            continue;
        }

        System.out.println("finished");
        //stopping threads
        for (mythread t : threads) {
            t.interrupt();
        }
        int choice = -1;
        while (choice != 0) {
            System.out.println("\n0 - exit");
            System.out.println("1 - write sorted array to random access file");
            System.out.println("2 - read random access file (contains unsorted array if not overwritten)");
            System.out.println("3 - view sorted array from variable\n");
            choice = input.nextInt();
            switch (choice) {
                case 0:
                    break;
                case 1: {
                    writeToMMF(ramFile, Arrays.toString(a));
                    break;
                }
                case 2:
                {
                    readFromMMF(ramFile);
                    break;
                }
                case 3:
                {
                    System.out.println(Arrays.toString(a));
                    break;
                }
            }
        }
    }
}

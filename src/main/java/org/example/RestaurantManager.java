package org.example;
import java.util.*;

// This is the class that manages the Reservation System for Jason's Restaurant
public class RestaurantManager {

    // Maximum Queue Capacity is specified here,
    // this can be reduced to test for scenarios where this Queue capacity exceeds
    final static int MAX_QUEUE_CAPACITY = 5000;

    //Maintain customer id incrementation in Queue for each entry
    private static int lastCustomerId = 0;

    public static void main(String[] args) {
        // Create a Scanner object
        Scanner scanner = new Scanner(System.in);

        // Create Customer List for Queue
        // Using LinkedList Queue implementation to maintain First In First Out
        Queue<Customer> queue = new LinkedList<>();

        // Create  Customer List for WaitList
        // Using LinkedHashSet to remove duplicate entries and to maintain Insertion Order
        Set<Customer> waitList = new LinkedHashSet<>();

        // Create  Customer List for ProcessedQueue
        // Using LinkedList Queue implementation to maintain First In First Out
        Queue<Customer> processedQueue = new LinkedList<>();

        boolean continueRunning = true;
        // Pre-populate Queue and Waitlist with random customers for testing limit exceed Queue function
        prePopulateCustomers(queue, waitList, 25); // Let's add 25 random customers
        while (continueRunning) {

            // Get the input customer details and add to the Queue
            System.out.println("\nPlease enter details of a new customer to add to the Queue.");
            int id = generateNextCustomerId();
            String firstName = readName(scanner, "first name");
            String lastName = readName(scanner, "last name");
            String email = readEmail(scanner);

            Customer newCustomer = new Customer(id, firstName, lastName, email);
            addCustomerToQueue(newCustomer, queue, waitList, processedQueue);

            // For adding more Customers
            System.out.println("Do you want to add another user? (yes/no)");
            String answer = scanner.nextLine().trim().toLowerCase();
            if (!answer.equals("yes")) {
                continueRunning = false;
            }
        }

        // Printing Processed Queue
        System.out.println("\nProcessed queue: ");
        for (Customer customer : processedQueue) {
            System.out.println(customer);
        }

        // Printing Current Queue
        System.out.println("\nCurrent queue: ");
        for (Customer customer : queue) {
            System.out.println(customer);
        }

        // Printing Wait List
        System.out.println("\nWaitlist: ");
        for (Customer customer : waitList) {
            System.out.println(customer);
        }
    }

    //Reading First Name and Last Name of Customer
    //Validating the fistName field to contain only letters and no numbers or special characters
    private static String readName(Scanner scanner, String fieldName) {
        System.out.printf("Enter %s: ", fieldName);
        String name = scanner.nextLine();
        while (!name.matches("^[a-zA-Z]+[a-zA-Z\\s]*$")) {
            System.out.printf("Invalid %s. Please use alphabetic characters only: ", fieldName);
            name = scanner.nextLine();
        }
        return name.trim();
    }

    //Reading Email ID of Customer
    //Validating the Email ID field to have correct email format(@ and .)
    private static String readEmail(Scanner scanner) {
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        while (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            System.out.print("Invalid email format. Please enter a valid email: ");
            email = scanner.nextLine();
        }
        return email.trim();
    }

    // Increment and generate the next customer ID
    private static int generateNextCustomerId() {
        return ++lastCustomerId;
    }

    // Pre-populate Queue and Waitlist with random customers
    // Add in Queue, if Maximum Queue Capacity is not reached
    // Add in WaiList, if Maximum Queue Capacity is reached
    private static void prePopulateCustomers(Queue<Customer> queue, Set<Customer> waitList, int count) {
        for (int i = 0; i < count; i++) {
            int id = generateNextCustomerId();
            String firstName = "CustomerName" + id;
            String lastName = "CustomerLast" + id;
            String email = "customer" + id + "@gmail.com";

            Customer randomCustomer = new Customer(id, firstName, lastName, email);

            // Randomly add the customer to either queue or waitlist
            if (queue.size() < MAX_QUEUE_CAPACITY) {
                queue.add(randomCustomer);
            } else {
                waitList.add(randomCustomer);
            }
        }
    }

    // Adding New Customers to Queue if queue size is not full, else to Wait list
    // We make use of synchronized here, to avoid concurrency issues.
    // Covers the edge scenario that even if a many employers are trying to manage queue simultaneously, no issue occurs.
    public static synchronized void addCustomerToQueue(Customer customer, Queue<Customer> queue, Set<Customer> waitList, Queue<Customer> processedQueue) {
        // Checks if Customer is already present in Queue and Wait List to avoid duplicates
        if (!queue.contains(customer) && !waitList.contains(customer)) {
            // Add to the queue if queue size is not full
            if (queue.size() < MAX_QUEUE_CAPACITY) {
                queue.offer(customer);
                System.out.println("Customer " + customer.getFirstName() + " added to the queue!");
            } else {
                // Queue is full, so add the Customer to Wait list
                waitList.add(customer);
                System.out.println("Queue is full! " + customer.getFirstName() + " added to waitList.");
            }
        } else {
            System.out.println("Customer already exists in the Queue or Waitlist.");
        }

        // After adding Customer to the appropriate place, checking if we can move a customer
        // from the waitList to the main queue, if there is space in main Queue
        fillQueueFromWaitList(queue, waitList, processedQueue);
    }

    // Move Customers from WaitList to Queue
    // Move Customers from Queue to Processed Queue
    private static void fillQueueFromWaitList(Queue<Customer> queue, Set<Customer> waitList, Queue<Customer> processedQueue) {
        // Check if main queue is not full and wait list is not empty
        while (queue.size() < MAX_QUEUE_CAPACITY && !waitList.isEmpty()) {
            // If so, move the first customer from the wait list to the main queue
            Iterator<Customer> iterator = waitList.iterator();
            if (iterator.hasNext()) {
                Customer waitListCustomer = iterator.next();
                queue.offer(waitListCustomer); // Add to the main queue
                iterator.remove(); // Remove from wait list

                System.out.println("Moved customer from waitList to queue: " + waitListCustomer.getFirstName());
            }
        }

        // If the queue is full, remove the first customer in the queue and add to processed Queue
        // Persist the Customers in processed Queue in DB (MS SQL DB used here)
        if (queue.size() == MAX_QUEUE_CAPACITY) {
            Customer processedCustomer = queue.poll(); // Remove the first customer from the queue
            processedQueue.offer(processedCustomer);// Add to the processed queue
            System.out.println("Processed and moved to processed queue: " + processedCustomer.getFirstName());

            // Persist the Customers in processed Queue in DB
            DbUtilities.insertProcessedCustomers(processedQueue);

        }
    }
}

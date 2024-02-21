Management of local karaoke restaurant

To setup a Local Karaoke Resturant:

Here we are maintaining 3 different queues.
1. Current Queue - This Linked List Queue maintains the Customer queue list in First In First Out order and has a maximum capacity of 5000.
2. WaitList - This is a Set implementation used to maintain customers in WaitList, to avoid duplicate customers and add the customers to waitList as soon as the current Queue becomes filled.
3. Processed Queue - This Linked List Queue maintains the Processed queue list and moved the customers from Current Queue once it gets filled and then persists this in DB.
                     Here, I have used MS SQL Server for persisting in DB.
                     We can use any databases as per requirement to persist the processed customer details.

**RestaurantManager:**
We have a User interactive console, which gets customer input from the employer.
Here, we enter the details of the Customer,
- customer id (auto incremented and generated)
- firstName 
- last Name
- email
All these details are mentioned in the **Customer class**. Which maintains constructor, getters and setters.
We validate the email id of a customer to make sure it is unique and no duplicate records of a customer is maintained in the queues, every time a new customer detail is added.
Validation for firstName and last name (alphabets only) and email (correct email format with @ and .) is also verified before inserting elements in queues.

**addCustomerToQueue()**
Checks if Customer is already present in Queue and Wait List to avoid duplicates.
Add Customer to the Current queue if queue size is not full.
If Current Queue is full, add the Customer to Wait list.
Here we make use of synchronized here, to avoid concurrency issues.
This covers the edge scenario that even if a many employers are trying to manage queue simultaneously, no issue occurs.

**fillQueueFromWaitList()**
Check if Current queue is not full and wait list is not empty.
If so, move the first customer from the wait list to the main queue.
If the queue is full, remove the first customer in the queue and add to processed Queue.
Persist the Customers in processed Queue in DB (MS SQL DB used here).

**Random Customer for edge case - prePopulateCustomers() (Current Queue limit exceeds):**
We have also used the functionality to generate random customers to test edge case scenarios when the Current Queue limit exceeds.
In this case, we add the incoming new Customers to the WaitList.
Once the queue is full, we also move the first element in the current Queue, and store it in Processed Queue. From there we persist the customer details and insert to the DB table Customers.

**DbUtilities class**
Maintains connection to MS SQL DB using DB connection URL, where we pass the SQL servername with port, username, password and database name.
We then take values from Processed Queue and add insert the Customers to to Customers DB table.


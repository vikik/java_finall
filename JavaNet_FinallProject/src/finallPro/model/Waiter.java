package finallPro.model;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Waiter extends Thread{
	private static Logger theLogger = Logger.getLogger("myLogger");
	private FileHandler waiterHandler;
	private Restaurant theResturant;
	private String name;
	private int maxCustomersCuncurrentPerWaiter;
	private int servedCustomerNum;
	private ExecutorService customersEs;
	private Vector<Customer> allCustomers = new Vector<Customer>();
	private Queue<Customer> waitingCustomers = new LinkedList<Customer>();
	private boolean reachedMaxFirstTime = true;
	private boolean stop = false;
	
	public Waiter(String name, Restaurant theResturant) {
		super();
		this.name = name;
		this.theResturant = theResturant;
		this.servedCustomerNum = 0;
		
		try {
			waiterHandler = new FileHandler("Waiter_" + name + ".txt");
			waiterHandler.setFilter(new WaiterFilter(this.name));
			waiterHandler.setFormatter(new MyFormatter());
			theLogger.addHandler(waiterHandler);
			theLogger.setUseParentHandlers(false);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return "Waiter id = " + this.getId() + " name = " + name;
	}

	public void customerDone(Customer c){
		theLogger.log(Level.INFO, " Waiter #" + getId() + " " + this.name + " reported that customer " 
				+ c.getTheName() + " paid his check", this);
		theResturant.customerDone(c);
		allCustomers.remove(c);
	}
	
	public int getCurrentServedCustomerNum(){
		return this.allCustomers.size();
	}
	
	public int getServedCustomerNum(){
		return this.servedCustomerNum;
	}
	
	public String getTheName() {
		return name;
	}

	public void setTheName(String name) {
		this.name = name;
	}

	public Restaurant getTheResturant() {
		return theResturant;
	}

	public void setTheResturant(Restaurant theResturant) {
		this.theResturant = theResturant;
	}

	public int getMaxCustomersCuncurrentPerWaiter() {
		return maxCustomersCuncurrentPerWaiter;
	}

	public void setMaxCustomersCuncurrentPerWaiter(int maxCustomersCuncurrentPerWaiter) {
		this.maxCustomersCuncurrentPerWaiter = maxCustomersCuncurrentPerWaiter;

		this.customersEs = Executors.newFixedThreadPool(maxCustomersCuncurrentPerWaiter);
	}

	public void addCustomer(Customer newCustomer) {
		theLogger.log(Level.INFO, " Waiter #" + getId() + " " + this.name 
				+ " will serve customer " + newCustomer.getTheName(), this);
		servedCustomerNum += 1;
		allCustomers.add(newCustomer);
		customersEs.execute(newCustomer);
	}
	
	public void endShift() {
		if(reachedMaxFirstTime && servedCustomerNum == theResturant.getMaxCustomersPerShift()){
			theLogger.log(Level.INFO, " Waiter #" + getId() + " " + this.name 
					+ "has reached maximum customers per shift", this);
			reachedMaxFirstTime = false;
		}
		customersEs.shutdown();
		synchronized (/*dummyWaiter*/this) {
			/*dummyWaiter.*/notifyAll();
		}
	}

	public synchronized void addWaitingCustomer(Customer c) {
		waitingCustomers.add(c);

		theLogger.log(Level.INFO, " Waiter #" + getId() + " " + this.name 
				+ ": After adding customer " + c.getTheName() + " there are " 
				+ waitingCustomers.size() + " customers waiting", this);

		synchronized (/*dummyWaiter*/this) {
			if (waitingCustomers.size() == 1) {
				/*dummyWaiter.*/notify(); // to let know there is an customer
										// waiting
			}
		}
	}

	public synchronized void notifyCustomer() {
		Customer firstCustomer = waitingCustomers.poll();
		if (firstCustomer != null) {
			int stage = firstCustomer.getStage();
			if(stage == 2){
				theLogger.log(Level.INFO, " Waiter #" + getId() + " " + this.name 
						+ " is ordering the dish for customer " 
						+ firstCustomer.getTheName(), this);
				int dishId = theResturant.orderDish(this, firstCustomer);
				firstCustomer.setDishId(dishId);
				theLogger.log(Level.INFO, " Waiter #" + getId() + " " + this.name 
						+ " is notifying customer " + firstCustomer.getTheName()
						+ " that dish " + dishId + " is ready", this);
			}
			if(stage == 3){
				theLogger.log(Level.INFO, " Waiter #" + getId() + " " + this.name 
						+ " is bringing the check to customer " + firstCustomer.getTheName()
						+ " . the check is for " + theResturant.getCheck(firstCustomer.getDishId()) + " Shekels", this);
			}
			else 
				theLogger.log(Level.INFO, " Waiter #" + getId() + " " + this.name 
						+ " is notifying customer " + firstCustomer.getTheName() 
						+ " at stage " + firstCustomer.getStage(), this);
			synchronized (firstCustomer) {
				firstCustomer.notifyAll();
			}
		}
			try {
				theLogger.log(Level.INFO, " Waiter #" + getId() + " " + this.name 
						+ " waits that customer " + firstCustomer.getTheName() 
						+ " will announce it is finished", this);

				wait(); // wait till the customer finishes

				theLogger.log(Level.INFO, " Waiter #" + getId() + " " + this.name 
						+ " was announced that customer " + firstCustomer.getTheName() 
						+ " is finished", this);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	public void run() {
		theResturant.waiterStartShift(this);
		
		theLogger.log(Level.INFO, " Waiter #" + getId() + " " + this.name 
				+ " Strated its shift", this);
		theResturant.setNumOfWaitersWorkedToday(theResturant.getNumOfWaitersWorkedToday() + 1);
		while (!customersEs.isTerminated() && !stop) {
			if (!waitingCustomers.isEmpty()) {
				notifyCustomer();
			} else {
				synchronized (/*dummyWaiter*/this) {
					try {
						if(customersEs.isTerminated())
							break;
						theLogger.log(Level.INFO, " Waiter #" + getId() + " " + this.name 
								+ " has no customers waiting", this);
						/*dummyWaiter.*/wait(); // wait till there is an customer
											// waiting
						theLogger.log(Level.INFO, " Waiter #" + getId() + " " + this.name 
								+ " recieved a message there is an customer waiting", this);
					} catch (InterruptedException e) {
						theLogger.log(Level.INFO, " Waiter #" + getId() + " " + this.name 
								+ " didn't reached maximum customers per shift,"
								+ " ending shift due to restaurant closure", this);
						customersEs.shutdownNow();
						stop = true;
					}
				}
			}
		}
		theLogger.log(Level.INFO, " Waiter #" + getId() + " " + this.name 
				+ " Ended its shift", this);
		theResturant.waiterEndShift(this);
	}
}
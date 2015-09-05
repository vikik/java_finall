package finallPro.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Restaurant extends Thread{
	private static Logger theLogger = Logger.getLogger("myLogger");
	private FileHandler restHandler;
	private String name;
	private int numOfSeats;
	private int maxCustomersPerDay;
	private int maxWaitersInShift;
	private int maxCustomersCuncurrentPerWaiter;
	private int maxCustomersPerShift;
	private boolean warning = true;
	private int numOfWaitersWorkedToday = 0;
	private int numOfServedCustomersToday = 0;
	protected final static int MINIMUM = 1000;
	protected final static int MAXIMUM = 10000;
	private Kitchen kitchen = new Kitchen();
	private Vector<Waiter> workingWaiters = new Vector<Waiter>();
	private Vector<Customer> allCustomers = new Vector<Customer>();
	private Vector<Customer> diningCustomers = new Vector<Customer>();
	private Queue<Customer> waitingCustomers = new LinkedList<Customer>();
	private HashMap<Integer, Integer> dishCost = new HashMap<>();
	private List<Future<Integer>> allOrders = new ArrayList<Future<Integer>>();
	private ExecutorService kitchEs;
	private ExecutorService waitersEs;
	
	//Restaurant c'tor
	public Restaurant(String name, int maxCustomersPerDay, int numOfSeats, 
			int maxWaitersInShift, int maxCustomersCuncurrentPerWaiter, int maxCustomersPerShift) {
		super();
		this.name = name;
		this.numOfSeats = numOfSeats;
		this.maxCustomersPerDay = maxCustomersPerDay;
		this.maxWaitersInShift = maxWaitersInShift;
		this.maxCustomersCuncurrentPerWaiter = maxCustomersCuncurrentPerWaiter;
		this.maxCustomersPerShift = maxCustomersPerShift;
		this.kitchEs = Executors.newFixedThreadPool(maxCustomersPerDay);
		this.waitersEs = Executors.newFixedThreadPool(maxWaitersInShift);
		
		try {
			restHandler = new FileHandler("Resturant_" + name + ".txt");
			restHandler.setFormatter(new MyFormatter());
			theLogger.addHandler(restHandler);
			theLogger.setUseParentHandlers(false);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	//Getter and Setters
	public String getTheName() {
		return name;
	}

	public void setTheName(String name) {
		this.name = name;
	}

	public int getNumOfSeats() {
		return numOfSeats;
	}

	public void setNumOfSeats(int numOfSeats) {
		this.numOfSeats = numOfSeats;
	}

	public int getMaxCustomersPerDay() {
		return maxCustomersPerDay;
	}

	public void setMaxCustomersPerDay(int maxCustomersPerDay) {
		this.maxCustomersPerDay = maxCustomersPerDay;
	}
	
	public int getMaxWaitersInShift() {
		return maxWaitersInShift;
	}

	public void setMaxWaitersInShift(int maxWaitersInShift) {
		this.maxWaitersInShift = maxWaitersInShift;
	}

	public int getMaxCustomersCuncurrentPerWaiter() {
		return maxCustomersCuncurrentPerWaiter;
	}

	public void setMaxCustomersCuncurrentPerWaiter(int maxCustomersCuncurrentPerWaiter) {
		this.maxCustomersCuncurrentPerWaiter = maxCustomersCuncurrentPerWaiter;
	}

	public int getMaxCustomersPerShift() {
		return maxCustomersPerShift;
	}

	public void setMaxCustomersPerShift(int maxCustomersPerShift) {
		this.maxCustomersPerShift = maxCustomersPerShift;
	}
	
	public int getNumOfWaitersWorkedToday() {
		return numOfWaitersWorkedToday;
	}

	public void setNumOfWaitersWorkedToday(int numOfWaitersWorkedToday) {
		this.numOfWaitersWorkedToday = numOfWaitersWorkedToday;
	}

	public int getNumOfServedCustomersToday() {
		return numOfServedCustomersToday;
	}

	public void setNumOfServedCustomersToday(int numOfServedCustomersToday) {
		this.numOfServedCustomersToday = numOfServedCustomersToday;
	}

	//Activities allowed at the restaurant for waiting customers
	@AllowedActivity (msg = "This activity is allowed")
	public void readNewsPaper(){
	}
	
	@AllowedActivity (msg = "This activity is allowed")
	public void playCandycrush(){
	}
	
	@AllowedActivity (msg = "This activity is allowed")
	public void playBubbles(){
	}
	
	public void talkOnThePhone(){
	}
	
	public void watchMovie(){
	}
	
	@AllowedActivity (msg = "This activity is allowed")
	public void doHomework(){
	}
	//get current total income
	public int getTotalIncome() {
		int totalIncome = 0;
		for (int orderPrice : dishCost.values()) {
		    totalIncome+=orderPrice;
		}
		return totalIncome;
	}
	//get map of all dishes and prices
	public HashMap<Integer,Integer> getIncomes() {
		return dishCost;
	}
	//get iterable list of all waiting customers
	public Iterable<Customer> getWaitingCustomersList() {
		return waitingCustomers;
	}
	//get iterable list of all dining customers
	public Iterable<Customer> getDiningCustomers() {
		return diningCustomers;
	}
	//get the check for a specific order id
	public int getCheck(int dishId) {
		return dishCost.get(dishId);
	}
	//notify the restaurant that the customer is done
	public void customerDone(Customer c){
		try {
			c.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		diningCustomers.remove(c);
		theLogger.log(Level.INFO, " Restaurant " + this.name + ": customer #" + c.getId() + " " 
				+ c.getTheName() + " paid his check and leaves the restaurant", this);
		warning = true;
	}
	//operation order dish, connect the waiter with the kitchen
	public int orderDish(Waiter waiter, Customer customer){
		int dishId = -1;
		theLogger.log(Level.INFO, " Restaurant " + this.name + ": waiter #" + waiter.getId()
		+ " " + waiter.getTheName() + " order a dish from the kitchen for customer #" 
				+customer.getId() + " " + customer.getTheName(), this);
		Future<Integer> current = null;
		try{
			current = kitchEs.submit(kitchen.makeDish());
		}catch(RejectedExecutionException e) {}
		try {
			dishId = current.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		allOrders.add(current);
		theLogger.log(Level.INFO, " Restaurant " + this.name + ": is notifying waiter #" 
		+ waiter.getId() + " " + waiter.getTheName() + " that dish " + dishId 
		+ " ordered by customer #" + customer.getId() + " " + customer.getTheName() + " is ready", this);
		
		dishCost.put(dishId, (int) Math.floor(Math.random() * Math.sqrt(Restaurant.MAXIMUM)));
		
		return dishId;
	}

	public boolean addWaiter(Waiter newWaiter) {
		try{
			theLogger.log(Level.INFO, " Restaurant " + this.name + ": adding waiter #" + newWaiter.getId()
					+ " " + newWaiter.getTheName() + " to waiters waiting list", this);
			newWaiter.setMaxCustomersCuncurrentPerWaiter(maxCustomersCuncurrentPerWaiter);
			waitersEs.execute(newWaiter);
		}
		catch(Exception e){return false;}
		return true;
	}
	
	public boolean addCustomer(Customer newCustomer) {
		if(allCustomers.size() < maxCustomersPerDay){
			theLogger.log(Level.INFO, " Restaurant " + this.name + ": Customer #" + newCustomer.getId() 
					+ " " + newCustomer.getTheName() + " has entered the restaurant and "
							+ "added to customers waiting list", this);
			allCustomers.add(newCustomer);
			waitingCustomers.add(newCustomer);
			return true;
		}
		else {
			theLogger.log(Level.INFO, " Restaurant " + this.name + ": Sorry customer #" + newCustomer.getId() 
					+ " " + newCustomer.getTheName() + " , we don't get anymore customers today", this);
			return false;
		}
	}
	
	public void waiterStartShift(Waiter w){
		theLogger.log(Level.INFO, " Restaurant " + this.name + ": waiter #" + w.getId() + " " 
				+ w.getTheName() + " starting his shift", this);
		workingWaiters.add(w);
	}

	public void waiterEndShift(Waiter w){
		theLogger.log(Level.INFO, " Restaurant " + this.name + ": waiter #" + w.getId() + " " 
				+ w.getTheName() + " ended his shift", this);
		workingWaiters.remove(w);
	}
	
	public void closeResturant() {
		theLogger.log(Level.INFO, " Restaurant " + this.name + ": is in closing process", this);
		
		if(waitingCustomers.size() > 0){
			theLogger.log(Level.INFO, " Restaurant " + this.name + ": There were "
					+ "still customers on the waiting list - The hostess sent them home", this);
			waitingCustomers.clear();
		}
		if(workingWaiters.size() > 0){
			for(int i = 0; i < workingWaiters.size(); i++)
				workingWaiters.get(i).endShift();
		}
		while(diningCustomers.size() > 0); //wait for all dining customers to finish
		kitchEs.shutdown();
		waitersEs.shutdownNow();
		synchronized (/*dummyWaiter*/this) {
			/*dummyWaiter.*/notifyAll();
		}
	}
	//notify customer if there is a free seat and a waiter
	public synchronized void notifyCustomer() {
		Waiter tmp = null;
		try {
			tmp = workingWaiters.get(0);
		}catch(ArrayIndexOutOfBoundsException e) {
			theLogger.log(Level.INFO, " Restaurant " + this.name + ": No workinr waiters", this);
			return;
		}
		if(diningCustomers.size() < numOfSeats){
			for(int i = 0; i < workingWaiters.size(); i++)
				if(tmp.getCurrentServedCustomerNum() > workingWaiters.get(i).getCurrentServedCustomerNum())
					tmp = workingWaiters.get(i);
			if(tmp.getCurrentServedCustomerNum() < tmp.getMaxCustomersCuncurrentPerWaiter() && 
					tmp.getServedCustomerNum() < maxCustomersPerShift){
				Customer firstCustomer = waitingCustomers.poll();
				theLogger.log(Level.INFO, " Restaurant " + this.name + ": seated customer " + 
						firstCustomer.getTheName() + " and assigned him " + tmp.getTheName()
						+ " to be his waiter", this);
				firstCustomer.setTheWaiter(tmp);
				diningCustomers.add(firstCustomer);
				tmp.addCustomer(firstCustomer);
				numOfServedCustomersToday++;
			}
			else if(warning){
				theLogger.log(Level.SEVERE, " Restaurant " + this.name + " Important message to "
						+ "manager: There are seats available but no available waiters!!", this);
				warning = false;
			}
		}
	}

	public void run() {
		theLogger.log(Level.INFO, " Restaurant " + this.name + ": is open", this);
		while (!waitersEs.isTerminated()) {
			if (!waitingCustomers.isEmpty())
				notifyCustomer();
			for(int i = 0; i < workingWaiters.size(); i++){
				try{
					if(workingWaiters.get(i).getServedCustomerNum() == maxCustomersPerShift)
						workingWaiters.get(i).endShift();
				}
				catch(ArrayIndexOutOfBoundsException e){}
			}
		}
		theLogger.log(Level.INFO, " Restaurant " + this.name + ": is closed", this);
		
		for (Handler h : theLogger.getHandlers()) {
			  h.close();
		}
	}
}

package finallPro.model;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.reflect.Method;

public class Customer extends Thread{
	private static Logger theLogger = Logger.getLogger("myLogger");
	private FileHandler customerHandler;
	private String name;
	private String activityWhileWaiting;
	private Waiter theWaiter;
	private int stage;
	private int dishId;

	public Customer(String name, String whileWaiting) {
		super();
		this.name = name;
		this.activityWhileWaiting = whileWaiting;

		try {
			customerHandler = new FileHandler("Customer_" + name + ".txt");
			customerHandler.setFilter(new CustomerFilter(this.name));
			customerHandler.setFormatter(new MyFormatter());
			theLogger.addHandler(customerHandler);
			theLogger.setUseParentHandlers(false);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "Customer id = " + this.getId() + " name = " + name;
	}

	public String getTheName() {
		return name;
	}

	public void setTheName(String name) {
		this.name = name;
	}

	public String getActivityWhileWaiting() {
		return activityWhileWaiting;
	}

	public void setActivityWhileWaiting(String whileWaiting) {
		this.activityWhileWaiting = whileWaiting;
	}

	public Waiter getTheWaiter() {
		return theWaiter;
	}

	public void setTheWaiter(Waiter theWaiter) {
		this.theWaiter = theWaiter;
	}

	public int getStage() {
		return stage;
	}

	public void setStage(int stage) {
		this.stage = stage;
	}
	
	public void setDishId(int dishId) {
		this.dishId = dishId;
	}
	
	public int getDishId() {
		return dishId;
	}
	
	public void whileWaiting(){
		Method[] allMethods = Restaurant.class.getDeclaredMethods();

		for (Method m : allMethods){
			if(m.getName().equals(activityWhileWaiting) && m.isAnnotationPresent(AllowedActivity.class))
			{
				theLogger.log(Level.INFO, name + " is " + activityWhileWaiting, this);
			}
			else if (m.getName().equals(activityWhileWaiting) && !(m.isAnnotationPresent(AllowedActivity.class)))
			{
				theLogger.log(Level.INFO, activityWhileWaiting + " is now allowed. " + name + " just waiting.", this);
			}
			
		}
	}

	private void waitForMenue() throws InterruptedException {
		synchronized (this) {
			theWaiter.addWaitingCustomer(this);
			stage = 0;
			theLogger.log(Level.INFO, " Customer #" + getId() + " " + this.name 
					+ " is waiting for the menu (from waiter " + theWaiter.getTheName() + ")", this);
			wait();
		}
		synchronized (theWaiter) {
			theLogger.log(Level.INFO, " --> Customer #" + getId() + " " + this.name 
					+ " got the menu from " + theWaiter.getTheName() + " and started looking at the menu", this);
			
			Thread.sleep((int) (Math.floor(Math.random() * Restaurant.MAXIMUM) + Restaurant.MINIMUM));
			
			theLogger.log(Level.INFO, " <-- Customer #" + getId() + " " + this.name 
					+ " is ready to order (from waiter " + theWaiter.getTheName() + ")", this);
		
			stage = 1;
			theWaiter.notifyAll();
		}
	}

	private void makeOrder() throws InterruptedException {
		synchronized (this) {
			theWaiter.addWaitingCustomer(this);
			
			theLogger.log(Level.INFO, " Customer #" + getId() + " " + this.name 
					+ " is waiting to order (from waiter " + theWaiter.getTheName() + ")", this);
			wait();
		}
		synchronized (theWaiter) {
			theLogger.log(Level.INFO, " --> Customer #" + getId() + " " + this.name 
					+ " is ordering (from waiter " + theWaiter.getTheName() + ")", this);
			
			Thread.sleep((int) (Math.floor(Math.random() * Restaurant.MAXIMUM) + Restaurant.MINIMUM));
			
			theLogger.log(Level.INFO, " <-- Customer #" + getId() + " " + this.name 
					+ " finished order (from waiter " + theWaiter.getTheName() + ")", this);
		
			stage = 2;
			theWaiter.notifyAll();
		}
	}
	
	private void eat() throws InterruptedException {
		synchronized (this) {
			theWaiter.addWaitingCustomer(this);
			
			theLogger.log(Level.INFO, " Customer #" + getId() + " " + this.name 
					+ " is waiting for the dish (from waiter " + theWaiter.getTheName() + ")", this);
			
			whileWaiting();
			wait();
		}
		synchronized (theWaiter) {
			theLogger.log(Level.INFO, " --> Customer #" + getId() + " " + this.name 
					+ " got the dish (from waiter " + theWaiter.getTheName() + ") and started eating", this);
						
			Thread.sleep((int) (Math.floor(Math.random() * Restaurant.MAXIMUM) + Restaurant.MINIMUM));
			
			theLogger.log(Level.INFO, " <-- Customer #" + getId() + " " + this.name 
					+ " finished eating and is asking for the check (from waiter " + theWaiter.getTheName() + ")", this);
			
			stage = 3;
			theWaiter.notifyAll();
		}
	}
	
	private void waitForCheck() throws InterruptedException {
		synchronized (this) {
			theWaiter.addWaitingCustomer(this);
			theLogger.log(Level.INFO, " Customer #" + getId() + " " + this.name 
					+ " is waiting for the check (from waiter " + theWaiter.getTheName() + ")", this);
			
			wait();
		}
		synchronized (theWaiter) {
			theLogger.log(Level.INFO, " --> Customer #" + getId() + " " + this.name 
					+ " got the check (from waiter " + theWaiter.getTheName() + ")", this);
			
			Thread.sleep((int) (Math.floor(Math.random() * Restaurant.MAXIMUM) + Restaurant.MINIMUM));
			
			theLogger.log(Level.INFO, " <-- Customer #" + getId() + " " + this.name 
					+ " pay + tip for the waiter (to waiter " + theWaiter.getTheName() + ")", this);
		
			stage = 4;
			theWaiter.notifyAll();
		}
	}

	@Override
	public void run() {
		try {
			waitForMenue();
			makeOrder();
			eat();
			waitForCheck();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		theWaiter.customerDone(this);
		theLogger.log(Level.INFO, " Customer #" + getId() + " " + this.name 
				+ " is leaving the restaurant", this);
	}
}

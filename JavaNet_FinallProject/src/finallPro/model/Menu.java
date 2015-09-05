package finallPro.model;

import java.util.HashMap;
import java.util.Scanner;

public class Menu {
	static boolean isOpen = true;
	
	static public void createMenu(Restaurant rest){
		
		boolean menuOn = true;
		boolean checkInput = true;
		String input;
		int ch = 0;
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);

		do{
			
			System.out.println("Action Menu: \n\n"
					+ "Enter 1 to Add Customer \n"
					+ "Enter 2 to Add Waiter \n"
					+ "Enter 3 to View Waiting Customer List \n"
					+ "Enter 4 to View Table Info \n"
					+ "Enter 5 to View Resturant Incomes \n"
					+ "Enter 6 to End The Day and Exit \n");
			System.out.println("What would you like to do?");
			
			input = sc.nextLine();
			
			while(checkInput){
				try{
					ch = Integer.parseInt(input);
					checkInput = false;
				}
				catch(NumberFormatException e){
					System.out.println("Input not valid. Please try again:");
					input = sc.nextLine();
				}
			}
			switch (ch) {
			case 1:
				if(addCustomer(rest)) System.out.println("Customer was successfully added\n");
				else System.out.println("Restaurant is full: Customer was not added\n");
				break;

			case 2:
				if(addWaiter(rest)) System.out.println("Waiter was successfully added\n");
				else System.out.println("Error: Waiter was not added\n");
				break;

			case 3:
				showWaitingCustomers(rest);
				break;
				
			case 4:
				showTablesInfo(rest);
				break;
				
			case 5:
				showRestaurantIncomes(rest);
				break;
				
			case 6:
				closeRestaurant(rest);
				System.out.println("GoodBye!");
				menuOn = false;
				showStatistics(rest);
				break;
				
			default:
				System.out.println("Input not valid.\n");
			}
			checkInput = true;
		} while (menuOn);

	}

	private static void showStatistics(Restaurant rest) {
		System.out.println("\nStatistics: ");
		if(rest.isAlive())
			System.out.println("The restuarant is still in closing process");
		else System.out.println("The restuarant is closed");
		System.out.println("Total number of waiters who worked today: " + rest.getNumOfWaitersWorkedToday());
		System.out.println("Total number of customers who dined today: " + rest.getNumOfServedCustomersToday());
		System.out.println("Total incomes: " + rest.getTotalIncome());
	}

	private static void closeRestaurant(Restaurant rest) {
		if(!isOpen){
			System.out.println("Restuarant was already closed...");
			return;
		}
		Iterable<Customer> waitingCustomers = rest.getWaitingCustomersList();
		if(waitingCustomers.iterator().hasNext()){
			System.out.println("\nThere were still customers on the waiting list - The hostess sent them home");
		}
		System.out.println("\nPlease wait: Restaurant " + rest.getTheName() + " is in closing process...");
		rest.closeResturant();
		isOpen = false;
		try {
			rest.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Restaurant " + rest.getTheName() + " is now closed");
		/*
		new Thread(new Runnable() {
			@Override
			public void run() {
				rest.closeResturant();
				isOpen = false;
				try {
					rest.join();
					System.out.println("Restaurant " + rest.getTheName() + " is now closed");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		*/
	}

	private static void showRestaurantIncomes(Restaurant rest) {
		int sum = 0;
		HashMap<Integer,Integer> incomes = rest.getIncomes();
		System.out.println("\nRestaurant Incomes:\n");
		for (HashMap.Entry<Integer, Integer> entry : incomes.entrySet()) {
		    int orderId = entry.getKey();
		    int orderPrice = entry.getValue();
		    System.out.println("Order ID: " + orderId + "\tOrder Price: " + orderPrice);
		    sum+=orderPrice;
		}
		System.out.println("\n------------------------------------\n");
		System.out.println("Total Incomes: " + sum + "\n");
	}

	private static void showTablesInfo(Restaurant rest) {
		int count = 1;
		Iterable<Customer> diningCustomers = rest.getDiningCustomers();
		System.out.println("\nTables Information:\n");
		for(Customer c : diningCustomers){
			System.out.println("Table " + count + ":");
			System.out.println("\tSitting --> " + c + " Served by --> " + c.getTheWaiter());
			count++;
		}
		System.out.println();
	}

	private static void showWaitingCustomers(Restaurant rest) {
		Iterable<Customer> waitingCustomers = rest.getWaitingCustomersList();
		System.out.println("\nWaiting Customers List:\n");
		for(Customer c : waitingCustomers){
			System.out.println(c);
		}
		System.out.println();
	}

	private static boolean addWaiter(Restaurant rest) {
		if(!isOpen){
			System.out.println("Restuarant was already closed...");
			return false;
		}
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		String name = "";
		
		while(name.equals("")){
			System.out.println("Please enter waiter name: ");
			name = sc.nextLine();
		}
		return rest.addWaiter(new Waiter(name, rest));
	}

	private static boolean addCustomer(Restaurant rest) {
		if(!isOpen){
			System.out.println("Restuarant was already closed...");
			return false;
		}
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		String name = "";
		String whileWaiting = "";
		
		while(name.equals("")){
			System.out.println("Please enter customer name: ");
			name = sc.nextLine();
		}
		while(whileWaiting.equals("")){
			System.out.println("Please enter customer favorite activity while he waits: ");
			whileWaiting = sc.nextLine();
		}
		return rest.addCustomer(new Customer(name, whileWaiting));
	}
}

package finallPro.view;

import java.util.Scanner;

public class Menu {

	static public void createMenu(){
		
		int ch;
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);

		do{
			
			System.out.println("Choose action: \n"
					+ "Press 1 to Add Customer \n"
					+ "Press 2 to Add Waiter \n"
					+ "Press 3 to View Waiting Customer List \n"
					+ "Press 4 to View Table Info \n"
					+ "Press 5 to View Resturant Incomes \n"
					+ "Press 6 to End The Day \n\n"
					+ "press 0 to exit");
			
			ch = sc.nextInt();
			switch (ch) {
			case 1:
				System.out.println("You pressed 1 to Add Customer \n");
				break;

			case 2:
				System.out.println("You pressed 2 to Add Waiter");
				break;

			case 3:
				System.out.println("You pressed 3 to View Waiting Customer List");
				break;
			case 4:
				System.out.println("You pressed 4 to View Table Info");
				break;
			case 5:
				System.out.println("You pressed 5 to View Resturant Incomes");
				break;
			case 6:
				System.out.println("You pressed 6 to End The Day and view statistics");
				break;
			default:
				break;
			}
		} while (ch != 0);

	}
}
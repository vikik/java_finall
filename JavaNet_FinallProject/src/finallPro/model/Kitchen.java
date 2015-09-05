package finallPro.model;

import java.util.concurrent.Callable;

public class Kitchen {
	//empty c'tor
	public Kitchen(){}
	//create new thread with returned value dish
	public Callable<Integer> makeDish(){
		Callable<Integer> order = new Dish();
		return order;
	}
}

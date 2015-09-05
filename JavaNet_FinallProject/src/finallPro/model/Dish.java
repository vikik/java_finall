package finallPro.model;

import java.util.concurrent.Callable;

public class Dish implements Callable<Integer>{
	private static int idGenerator;
	private int id;
	
	public Dish() {
		this.id = ++idGenerator;
	}
	@Override
	public Integer call() throws Exception {
		//"make" new dish - sleep 1-10 secs, and then return a unique id
		Thread.sleep((int) (Math.floor(Math.random() * Restaurant.MAXIMUM) + Restaurant.MINIMUM));
		return this.id;
	}
}

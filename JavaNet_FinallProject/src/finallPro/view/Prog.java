package finallPro.view;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import finallPro.model.Customer;
import finallPro.model.Menu;
import finallPro.model.Restaurant;
import finallPro.model.Waiter;

public class Prog {

	public static void main(String[] args) {
		Restaurant resta = null;
		try {
			resta = readConf();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		if(resta != null)
			Menu.createMenu(resta);
		else System.out.println("Something is wrong with the configuration...");
		
	}
	public static Restaurant readConf() throws ParserConfigurationException, SAXException, IOException{
		File xmlFile = new File("src/RestConf.xml");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(xmlFile);
		
		doc.getDocumentElement().normalize();
		
		NodeList restNL = doc.getElementsByTagName("Resturant");
		Node restN = restNL.item(0);
		Element restE = (Element) restN;
		String restName = restE.getAttribute("name");
		int maxCustomersPerDay = Integer.parseInt(restE.getAttribute("maxCustomersPerDay"));
		int numOfSeats = Integer.parseInt(restE.getAttribute("numOfSeats"));
		
		NodeList waiterNL = doc.getElementsByTagName("Waiters");
		Node waiterN = waiterNL.item(0);
		Element waiterE = (Element) waiterN;
		int maxWaitersInShift = Integer.parseInt(waiterE.getAttribute("maxWaitersInShift"));
		int maxCustomersCuncurrentPerWaiter = Integer.parseInt(waiterE.getAttribute("maxCustomersCuncurrentPerWaiter"));
		int maxCustomersPerShift = Integer.parseInt(waiterE.getAttribute("maxCustomersPerShift"));
		
		Restaurant resta = new Restaurant(restName, maxCustomersPerDay, numOfSeats, 
				maxWaitersInShift, maxCustomersCuncurrentPerWaiter, maxCustomersPerShift);
		resta.start();
		
		Waiter[] ws = new Waiter[6];
		for(int i = 0; i < ws.length; i++){
			ws[i] = new Waiter("Waiter_" + i, resta);
			resta.addWaiter(ws[i]);
		}
		
		NodeList customerNL = doc.getElementsByTagName("Customer");
		
		Customer[] custs = new Customer[customerNL.getLength()];
		
		for(int i = 0; i < customerNL.getLength(); i++){
			Node customerN = customerNL.item(i);
			if (customerN.getNodeType() == Node.ELEMENT_NODE) {
				Element customerE = (Element) customerN;
				String custName = customerE.getAttribute("name");
				String custWhileWaiting = customerE.getAttribute("whileWaiting");
				
				custs[i] = new Customer(custName, custWhileWaiting);
				resta.addCustomer(custs[i]);
			}
		}
		return resta;
	}

}

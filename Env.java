package nl.uu.cs.is.apapl.environments.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import nl.uu.cs.is.apapl.apapl.Environment;
import nl.uu.cs.is.apapl.apapl.ExternalActionFailedException;
import nl.uu.cs.is.apapl.apapl.data.APLFunction;
import nl.uu.cs.is.apapl.apapl.data.APLIdent;
import nl.uu.cs.is.apapl.apapl.data.APLNum;
import nl.uu.cs.is.apapl.apapl.data.Term;

public class Env extends Environment {
    private final boolean log = true;
    
    private ArrayList<APLIdent> agents = new ArrayList<>();

    private int foodInEnvironment = 0;
    private HashMap<String, Integer> foodEachAgentHas = new HashMap<>();
    private HashMap<String, Boolean> agentWillSurviveTheDay = new HashMap<>();
    private int stock = 100;

    private int initializedAgentCounter = 0;
    private int totalNAgents = 3;
    
    public static void main(String [] args) {
    }

    protected void addAgent(String agName) {

        APLIdent aplagName = new APLIdent(agName);
        
        agents.add(aplagName);
        if (agName.equals("shopkeeper"))
            foodEachAgentHas.put(agName, 100);
        else
            foodEachAgentHas.put(agName, 0);
        log("env> agent " + agName + " has registered to the environment. It has " +
        	foodEachAgentHas.get(agName) + " food units.");
        
        initializedAgentCounter++;
    	if(initializedAgentCounter == totalNAgents) {
	        startDay();
	        growFood();
    	}
;
    }
    
    /*
     * EVENTS
     */
    protected void startDay() {
    	APLFunction event = new APLFunction("dayHasStarted", new APLNum(0));
    	agentWillSurviveTheDay.replaceAll((k,v)->v=false);
        throwEvent(event);
    }
    
    protected void growFood() {
    	int foodThatWillGrow = ThreadLocalRandom.current().nextInt(10, 20 + 1);
        APLFunction event = new APLFunction("foodHasGrown");
        foodInEnvironment += foodThatWillGrow;
        throwEvent(event);
    }

    
    /*
     * ACTIONS
     */
    public Term harvestFood(String agName) throws ExternalActionFailedException {
        log("env> agent " + agName + " tries to harvest food.");
        try {
        	APLNum harvestedAmount = new APLNum(foodInEnvironment);
            log("env> agent " + agName + " has harvested " + String.valueOf(foodInEnvironment) + " food units.");
            log("env> agent " + agName + " now has " +
            	String.valueOf(foodEachAgentHas.get(agName) + foodInEnvironment) + " food units.");
        	foodEachAgentHas.put(agName, foodEachAgentHas.get(agName) + foodInEnvironment);
        	foodInEnvironment = 0;
        	return harvestedAmount;
        } catch (Exception e) {
            //exception handling
            System.err.println("env> external action harvestFood() of " + agName + " failed: " + e.getMessage());
            return null;
        }
    }
    
    public Term consume(String agName, APLNum amount) throws ExternalActionFailedException {
        log("env> agent " + agName + " tries to consume " + amount + " food units.");
        try {
        	int foodTheAgentHas = foodEachAgentHas.get(agName);
        	int amountToConsume = amount.toInt();
            log("env> agent " + agName + " has " + String.valueOf(foodTheAgentHas) + " food units.");
            if (foodTheAgentHas < amountToConsume)
            	throw new ExternalActionFailedException("agent " + agName + " does not have enough food to eat!");
            else {
            	foodEachAgentHas.put(agName, foodTheAgentHas-amountToConsume);
                if (agName == "shopkeeper")
                	stock -= amountToConsume;
            	agentWillSurviveTheDay.put(agName, true);
                log("env> agent " + agName + " has consumed " + amountToConsume + " food units and now has " +
                	String.valueOf(foodEachAgentHas.get(agName)) + " food units.");
                return null;
            }
        } catch (Exception e) {
            //exception handling
            System.err.println("env> external action harvestFood() of " + agName + " failed: " + e.getMessage());
            return null;
        }
    }
    
    public Term giveFood(String agName, APLIdent receivingAgName, APLNum amount) throws ExternalActionFailedException {
        log("env> agent " + agName + " tries to give agent " + receivingAgName + " " + String.valueOf(amount) + " food units.");
        try {
        	int foodTheAgentHas = foodEachAgentHas.get(agName);
        	int amountInt = amount.toInt();
        	if (foodTheAgentHas < amountInt)
            	throw new ExternalActionFailedException("agent " + agName + " does not have enough food to give!");
        	else if (!foodEachAgentHas.containsKey(receivingAgName.getName()))
            	throw new ExternalActionFailedException("agent " + receivingAgName + " does not exist!");
        	else {
        		foodEachAgentHas.put(receivingAgName.getName(), foodEachAgentHas.get(receivingAgName.getName())+amountInt);
        		foodEachAgentHas.put(agName, foodEachAgentHas.get(agName)-amountInt);
                log("env> agent " + agName + " has now " + foodEachAgentHas.get(agName) + " food units.");
                log("env> agent " + receivingAgName.getName() + " has now " + foodEachAgentHas.get(receivingAgName.getName()) + " food units.");
                APLFunction event = new APLFunction("receivedFood", amount);
                throwEvent(event, receivingAgName.getName());
        	}
        } catch (Exception e) {
            //exception handling
            System.err.println("env> external action harvestFood() of " + agName + " failed: " + e.getMessage());
        }
    	return null;
    }
    
    public Term checkStock(String agName) throws ExternalActionFailedException {
    	try {
            log("env> agent " + agName + " checks the stock.");
            log("env> there are now " + stock + " food units in stock.");
            APLFunction event = new APLFunction("stockAvailable", new APLNum(stock));
            throwEvent(event);
    	} catch (Exception e) {
            //exception handling
            System.err.println("env> external action checkStock() of " + agName + " failed: " + e.getMessage());
        }
        return null;
    }
    
    public Term buy(String agName, APLNum amount) throws ExternalActionFailedException {
    	try {
        	int foodToBuy = amount.toInt();
            log("env> agent " + agName + " tries to buy " + foodToBuy + " apples.");
            APLFunction event = new APLFunction("bought", amount);
            throwEvent(event);
    	} catch (Exception e) {
            //exception handling
            System.err.println("env> external action checkStock() of " + agName + " failed: " + e.getMessage());
        }
        return null;
    }
    
    public Term sell(String agName, APLNum amount) throws ExternalActionFailedException {
    	try {
        	int foodToBuy = amount.toInt();
        	if (stock < foodToBuy)
        		throw new ExternalActionFailedException("Not enough food units in stock!");
            log("env> agent " + agName + " tries to sell " + foodToBuy + " food units.");
            stock -= foodToBuy;
        	foodEachAgentHas.put("villager", foodEachAgentHas.get("villager") + foodToBuy);
        	foodEachAgentHas.put(agName, foodEachAgentHas.get(agName) - foodToBuy);
            APLFunction event = new APLFunction("sold", amount);
            throwEvent(event);
    	} catch (Exception e) {
            //exception handling
            System.err.println("env> external action checkStock() of " + agName + " failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * External actions of agents can be caught by defining methods that have a Term as return value.
     * This method can be called by a 2APL agents as follows: \@env(square(5), X).
     * X will now contain the return value, in this case 25.
     * @param agName The name of the agent that does the external action
     * @param aplNum The num to calculate the square of, coded in an APLNum
     * @return The square of the input, coded in an APLNum
     */
    public Term square(String agName, APLNum aplNum) throws ExternalActionFailedException {
        int num = aplNum.toInt();

        log("env> agent " + agName + " requests the square of " + num + ".");
        log("env> Additional message because of yes");

        try {
            return new APLNum(num*num);

        } catch (Exception e) {
            //exception handling
            System.err.println("env> external action square() of " + agName + " failed: " +e.getMessage());
            return null;
        }
    }

    private void log(String str) {
        if (log) System.out.println(str);
    }
}

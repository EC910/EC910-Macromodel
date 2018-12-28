// This class is built following Lengnick(2013) paper on 
// Journal of Economic Behavior & Organization 86 (2013) 102– 120.


import java.util.ArrayList;

import jas.engine.Sim;
import jas.engine.SimEngine;
import jas.engine.SimModel;
import jas.engine.gui.JAS;
import jas.events.SimGroupEvent;

public class Model extends SimModel {

	//	Number of Households and Firm as of pg. 105
	private int H, F, num_typeA;

	//	containers (lists) for both households and firms
	private ArrayList<Household> HH_list;
	private ArrayList<Firm> FI_list;

	// connection matrices for typeA and typeB
	private boolean[][] matrix_A, matrix_B; 
	private int[][] matrix_A_constraints;

	//behavioral variables
	private double delta, phi_max, phi_min, alpha, Phi_max, Phi_min, 
	theta, Psi_price, Psi_quant, xi, pi, Theta;
	private int seed, beta, n, lambda, gamma;
	
	private double totalProduction=0.0;

	@Override
	public void setParameters() {
		// TODO Auto-generated method stub
		//		default values as of pg. 110;
		this.seed = 1;
		this.H = 1000;
		this.F = 100;
		this.num_typeA = 7;

		this.delta = .019;
		this.phi_max = 1;
		this.phi_min = .25;

		this.theta = 0.02;
		this.Phi_max = 1.15;
		this.Phi_min = 1.025;

		this.alpha = .9;

		this.Psi_price = 0.25;
		this.Psi_quant = 0.25;

		this.xi = 0.01;

		this.beta = 50;
		this.pi = 0.1;

		this.n = 7;
		this.gamma = 24;
		this.lambda = 3;
		
		this.Theta = 0.75;
		// open a probe to allow the user to modify default values
		Sim.openProbe(this, "Parameters Model");

	}

	@Override
	public void buildModel() {
		// TODO Auto-generated method stub
		
//		Initializes the random number generator with a specific seed.
//		Same seed value produces the same random number sequence
		Sim.getRnd().setSeed(seed);
		this.HH_list = new ArrayList<Household>();

		for(int i=0; i<this.H; i++){

			double w = Sim.getRnd().getNormal(1, .2);
			double m = Sim.getRnd().getNormal(1, .2);
			int c = Sim.getRnd().getIntFromTo(21, 105); // basic initialization of hh consumption 

			this.HH_list.add(new Household(i,w,m,c,this.num_typeA,this.alpha)); 
		}


		this.FI_list = new ArrayList<Firm>();


		for(int i=0; i<this.F; i++){

			double w = Sim.getRnd().getNormal(1, .2);
			int inv = Sim.getRnd().getIntFromTo(0, 10);
			double p = Sim.getRnd().getNormal(.1, .02);


			this.FI_list.add(new Firm(i,w,0,(int)(inv*0.9),
					(int)(inv*1.1),p, p*0.9, p*1.1,delta,
					this.Phi_min, this.Phi_max,
					this.phi_min, this.phi_max, 
					this.theta, this.lambda,this.gamma, this.Theta)); 
		}

		this.matrix_A = new boolean[this.H][this.F];
		this.matrix_B = new boolean[this.H][this.F];
		this.matrix_A_constraints = new int[this.H][this.F];

		for(int h=0; h<H; h ++) {

			//			typeB connection
			int newF = Sim.getRnd().getIntFromTo(0, F-1);
			this.matrix_B[h][newF] = true;
			this.HH_list.get(h).setTypeB(newF);//new
			this.FI_list.get(newF).getTypeB().add(h);


			// typeA connections
			int counter = 0;
			int[] newTypeA = new int[this.num_typeA];

			do {
				int f = Sim.getRnd().getIntFromTo(0, F-1);

				if(!this.matrix_A[h][f]){

					this.matrix_A[h][f] = true;

					this.HH_list.get(h).setEmployed(true);//new
					newTypeA[counter] = f;

					this.FI_list.get(f).getTypeA().add(h);

					counter++;
				}
			}
			while(counter<this.num_typeA);

			//			update typeA connections list to hh
			this.HH_list.get(h).setTypeA(newTypeA);

		}

		//		this.printConnections();
		this.scheduleEvents();


	}

	public void scheduleEvents(){

		SimGroupEvent beginningMonth, daily, endMonth;

		beginningMonth = eventList.scheduleGroup(0, 21);
		daily = eventList.scheduleGroup(0,1);
		endMonth = eventList.scheduleGroup(20,21);

		//		BEGINNING OF THE MONTH EVENTS
		beginningMonth.addCollectionEvent(FI_list, getObjectClass("Firm"), "newWage");
		// update demand for firms from previous month
		beginningMonth.addCollectionEvent(FI_list, getObjectClass("Firm"), "updateInvRange");
		// update marginal cost for firms
		beginningMonth.addCollectionEvent(FI_list, getObjectClass("Firm"), "updatePriceRange");
		beginningMonth.addCollectionEvent(FI_list, getObjectClass("Firm"), "updateDemandForLabour");

		beginningMonth.addEvent (this, "updateTypeA_Price");
		beginningMonth.addEvent (this, "updateTypeA_Quantity");

		beginningMonth.addEvent (this, "updateTypeB");
		
		beginningMonth.addEvent (this, "updateHouseholdsAveragePrices");

		//		DAILY EVENTS 
		daily.addEvent (this, "GoodMarketDailyEvents");
		daily.addCollectionEvent(FI_list, getObjectClass("Firm"), "produce");
		daily.addEvent(this,"QuantityProduced");

		//  END OF THE MONTH EVENTS 
		endMonth.addEvent (this, "firmsPayWages");
		endMonth.addEvent (this, "firmsPayProfits");

	}

	public void updateTypeA_Price(){

		for (int h = 0; h < this.H; h ++) {
			int f_index = Sim.getRnd().getIntFromTo(0,this.num_typeA-1);
			int f = this.HH_list.get(h).getTypeA()[f_index];
			int f_new;

			//			find a current firm to which h is not connected
			do {
				f_new = Sim.getRnd().getIntFromTo(0, this.matrix_A[0].length - 1);
			}
			while ( this.matrix_A[h][f_new]);



			double rnd = Sim.getRnd().getDblFromTo(0, 1);
			double price_f = this.FI_list.get(f).getP();
			double price_f_new = this.FI_list.get(f_new).getP();

			//			criteria to replace f with new_f
			if (rnd < this.Psi_price && (price_f - price_f_new)/price_f >= this.xi) {

				this.matrix_A[h][f] = false;
				this.matrix_A[h][f_new] = true;
				this.HH_list.get(h).setTypeA(f_new, f_index);

				this.FI_list.get(f).getTypeA().remove(new Integer(h));
				this.FI_list.get(f_new).getTypeA().add(h);


			}

		}

	}

	public void updateTypeA_Quantity(){

		for (int h = 0; h < this.H; h ++) {

			int tot_constraint = 0;
			int [] constraints = new int[this.num_typeA];

			for (int f_index = 0; f_index < this.num_typeA; f_index ++) {

				tot_constraint += this.matrix_A_constraints[h]
						[this.HH_list.get(h).getTypeA()[f_index]];

				constraints[f_index] = this.matrix_A_constraints[h]
						[this.HH_list.get(h).getTypeA()[f_index]];
			}

			if (tot_constraint>0) {

				double prob = Sim.getRnd().getDblFromTo(0, 1);
				int f_index = -1;
				double cumulativeProb = 0;


				//				find the firm to drop based on the prob of the constraints
				do{	

					f_index++;
					cumulativeProb += (double)this.matrix_A_constraints[h]
							[this.HH_list.get(h).getTypeA()[f_index]]/tot_constraint;	
					

				}while (prob>cumulativeProb && f_index <= this.num_typeA);

				//				find a new firm to connect to
				int f_new;

				do {
					f_new = Sim.getRnd().getIntFromTo(0, this.F - 1);
				}
				while ( this.matrix_A[h][f_new]);

				//				drop the link with f
				this.matrix_A[h][this.HH_list.get(h).getTypeA()[f_index]] = false;
				this.matrix_A[h][f_new] = true;

				this.HH_list.get(h).setTypeA(f_new, f_index);

				this.FI_list.get(this.HH_list.get(h).getTypeA()[f_index]).getTypeA().remove(new Integer(h));
				this.FI_list.get(f_new).getTypeA().add(h);


			}

		}

		//		reset the matrix of constraints
		this.matrix_A_constraints = new int [this.H][this.F];

	}

	public void updateTypeB() {

		int beta;
		double prob;
		
		try{
		
		
		for (int f=0; f<this.F; f ++){
			
			Firm firm = this.FI_list.get(f);
			
			for (int i=0; i<firm.getToFire(); i ++){
				
				int tofire = Sim.getRnd().getIntFromTo(0, firm.getTypeB().size()-1);
				this.HH_list.get(firm.getTypeB().get(tofire)).setEmployed(false);					
				firm.getTypeB().remove(tofire);
				
			}
			
			
		}

		for (int h = 0; h < this.H; h ++) {

			Household household = this.HH_list.get(h);
			Firm firm = null;

			if (household.employed){
				beta = 1;

				firm = this.FI_list.get(household.getTypeB());

				if (household.getW() <= firm.getW()) {
					prob = this.pi;
				}
				else {
					prob = 1;
				}
			}
			else {
				beta = this.beta;
				prob = 1;
			}

			//			probability check
			if (Sim.getRnd().getDblFromTo(0, 1)<prob) {

				//				search for new connection

				ArrayList<Firm> tempList = new ArrayList<Firm>();

				for (int i = 0; i < this.FI_list.size(); i ++) {
					tempList.add(this.FI_list.get(i));
				}

				if (household.employed) tempList.remove(firm);

				for (int i = 0; i < beta; i ++){

					int rnd = Sim.getRnd().getIntFromTo(0, tempList.size()-1);

					if (household.employed && tempList.get(rnd).getOpenPosition() > 0) {
						if(tempList.get(rnd).getW() > firm.getW()) {

							//							update typeB connections
							this.matrix_B[household.getId()][firm.getId()] = false;
							this.matrix_B[household.getId()][tempList.get(rnd).getId()] = true;

							household.setEmployed(true);
							household.setTypeB(tempList.get(rnd).getId());

							firm.getTypeB().remove(new Integer(household.getId()));
							tempList.get(rnd).getTypeB().add(household.getId());

							break;

						}
					}
					else {
						if(tempList.get(rnd).getW() >= household.getW() && tempList.get(rnd).getOpenPosition() > 0){

							//							update typeB connections
							this.matrix_B[household.getId()][tempList.get(rnd).getId()] = true;
							household.setEmployed(true);
							household.setTypeB(tempList.get(rnd).getId());

							if(firm != null && firm.getTypeB() != null && firm.getTypeB().size() > 0){
								firm.getTypeB().remove(new Integer(household.getId()));
							}
							tempList.get(rnd).getTypeB().add(household.getId());
							break;
						}
					}

					tempList.remove(rnd);

				}

			}
			

		}
		

		}catch(Exception e){
			e.printStackTrace();
		}


	}


	public void GoodMarketDailyEvents(){

		ArrayList<Household> HH_list_shuffle = new ArrayList<Household>();

		for (Household household : this.HH_list){
			HH_list_shuffle.add(household);	
		}

		do{

			Household household = HH_list_shuffle.get(Sim.getRnd().getIntFromTo(0, HH_list_shuffle.size()-1));


			ArrayList<Firm> tempList = new ArrayList<Firm>();

			for (int f_index = 0; f_index < this.num_typeA; f_index ++) {

				tempList.add(this.FI_list.get(household.getTypeA()[f_index]));
			}

			int purchased_quantity = 0;
			int household_demans = (int)(household.getC()/21);
			int visitedFirms = 0;


			while (household.getM() > 0 && 
					purchased_quantity/household_demans < 0.95 &&
					visitedFirms < this.n) {

				int f = Sim.getRnd().getIntFromTo(0, tempList.size()-1);
				Firm firm = tempList.get(f);

				firm.setD((firm.getD() + household_demans - purchased_quantity));

				//				check if firm has enough goods to satisfy the demand of the household
				int transaction_quantity = Math.min(household_demans - purchased_quantity, firm.getInv());

				//				goods constraints are updated
				this.matrix_A_constraints[household.getId()][firm.getId()] = household_demans- purchased_quantity - transaction_quantity;

				//				chech if the household has enough liquidity
				transaction_quantity = Math.min(transaction_quantity, (int)(household.getM()/firm.getP()));

				//				update firm inventory
				firm.setInv(firm.getInv() - transaction_quantity);
				//				update firm liquidity
				firm.setM(firm.getM() + transaction_quantity*firm.getP());

				//				update household liquidity
				household.setM(household.getM() - transaction_quantity*firm.getP());

				//				update the all purchased quantity
				purchased_quantity += transaction_quantity;

				//				System.out.println(household.toString() + "->" + firm.toString() + ": Q=" + transaction_quantity);

				//				remove f from the temp list of firms
				tempList.remove(firm);
				visitedFirms ++;
			}

			HH_list_shuffle.remove(household);

		}while(HH_list_shuffle.size()>0);



	}

	public void firmsPayWages () {

		for (Household h : this.HH_list) {

			Firm f = this.FI_list.get(h.getTypeB());

			double amountPaid = Math.min(f.getW(), f.getM()+f.getM_buffer());


			h.setM(h.getM() + amountPaid);


			double amountFromM = Math.min(f.getM(), amountPaid);
			double amountFromBuffer = Math.min(amountPaid-amountFromM, f.getM_buffer());

			f.setM(f.getM() - amountFromM);
			f.setM_buffer(f.getM_buffer() - amountFromBuffer);

		}
	}


	public void firmsPayProfits () {

		double aggregatedProfit = 0;
		double aggregatedHouseholdWealth = 0;

		for (Firm f : this.FI_list) {

			aggregatedProfit += f.getM();
			f.setM(0);

		}

		for (Household h : this.HH_list) {

			aggregatedHouseholdWealth += h.getM();

		}

		for (Household h : this.HH_list) {
			h.setDividend(aggregatedProfit * (h.getM()/aggregatedHouseholdWealth));
			h.setM(h.getM() + aggregatedProfit * (h.getM()/aggregatedHouseholdWealth));
		}


	}
	
	public void QuantityProduced(){
		double totalProduction=0.0;
		for(Firm f : this.FI_list){
			totalProduction += f.getDailyProduction();
		}
		this.totalProduction = totalProduction;
	}
	
	public double getTotalProduction(){
		return this.totalProduction;
	}
	
	public void updateHouseholdsAveragePrices () {

		for (Household h : this.HH_list) {
			
			double P = 0;
			int numFirms = 0;
			
			for (int f_index = 0; f_index < this.num_typeA; f_index ++) {

					
					P += this.FI_list.get(h.getTypeA()[f_index]).getP();

					
				
			}
			
			h.setP(P/this.num_typeA);

		}

	}


	public void printConnections() {

		for(int h=0; h<H; h++){

			System.out.print("HH" + h + " is connected with ");

			for(int f=0; f<F; f++){

				if(this.matrix_A[h][f]){
					//					System.out.println("HH" + h + " works for F" + f);

					System.out.print("F" + f + " ");
				}
			}

			System.out.println();
		}
	}
	
	public double getEmployment (){
		
		double employment = 0;
		
		for (Household h : this.HH_list) {

			if(h.isEmployed()) employment +=1;

		}
		
		return (employment/this.H)*100;
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		SimEngine eng = new SimEngine();
		JAS jas = new JAS(eng);
		jas.setVisible(true);

		Model m = new Model();
		eng.addModel(m);
		m.setParameters();
	}

	public ArrayList<Household> getHH_list() {
		return HH_list;
	}

	public ArrayList<Firm> getFI_list() {
		return FI_list;
	}

}

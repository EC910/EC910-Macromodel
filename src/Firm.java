import java.util.ArrayList;

import jas.engine.Sim;

// This class is built following Lengnick(2013) paper on 
// Journal of Economic Behavior & Organization 86 (2013) 102– 120.

public class Firm {
	

//	Id of the object
	private int id;
//	Offered wage as in pg.107
	private double w;
//	Liquitity as of pg. 106
	private double m;
//	inventory values for current, lower and upper bound respectively
//	as of pg. 107
	private int inv, inv_min, inv_max, d; 
//	price level for current, lower and upper bound respectively
//	as of pg. 107
	private double p, p_min, p_max;
	
//	behavioral parameters as in eq(5), (6), (7) respectively
	private double delta, Phi_min, Phi_max,phi_min, phi_max, theta, Theta;
	
//	marginal cost
	private double mc;
	
	private int openPosition, toFire, gamma, lambda, num_months_with_openpositions;
	
	private ArrayList<Integer> typeB;
	private ArrayList<Integer> typeA;
	
//	liquidity buffer for bad times, pg 109
	private double m_buffer;
	private double dailyProduces;
	
	
	public Firm(int id, double w, int inv, int inv_min, int inv_max, 
			double p, double p_min, double p_max, double delta,
			double Phi_min, double Phi_max,
			double phi_min, double phi_max, double theta, int lambda, int gamma, double Theta) {
		super();
		this.id = id;
		this.w = w;
		this.inv = inv;
		this.inv_min = inv_min;
		this.inv_max = inv_max;
		this.p = p;
		this.p_min = p_min;
		this.p_max = p_max;
		this.delta = delta;
		this.phi_min = phi_min;
		this.phi_max = phi_max;
		this.Phi_min = Phi_min;//new
		this.Phi_max = Phi_max;//new
		this.theta = theta;
		this.lambda = lambda;//new
		this.gamma = gamma;
		this.Theta = Theta;
		this.openPosition = 0;
		
		this.typeB = new ArrayList<Integer>();
		this.typeA = new ArrayList<Integer>();
	}
	
	// following eq(13-14) in main paper
	public void produce(){
		this.dailyProduces = this.lambda*this.getTypeB().size();
		this.inv += this.lambda*this.getTypeB().size();
		
	}
	
	public double getDailyProduction(){
		return dailyProduces;
	}
	
	
	// following eq(5) in main paper
	public void newWage(){
		
		this.w = this.w*(1+Sim.getRnd().getDblFromTo(-this.delta, this.delta));
	}
	
	// following eq(6) and (7) in main paper
	public void updateInvRange(){
		
		this.inv_max = (int)(this.Phi_max * this.d);
		this.inv_min = (int)(this.Phi_min * this.d);
	}
	
	public void updateDemandForLabour(){
		
		if (this.openPosition>0) this.num_months_with_openpositions ++;
		else this.w *=0.9; // the firm filled all open positions
//		therefore it reduces the wage
			
		if (this.num_months_with_openpositions == this.gamma){
			
			this.w *=1.1; // the firm had openpositions for gamma months, 
//			therefore it reduces the wage by 10%.
			
		}
		
		
		if(this.getInv() < this.getInv_min()){
//			hiring one worker
			this.openPosition ++;
			
//			consider to increase price
			if(this.p < this.p_max && Sim.getRnd().getDblFromTo(0, 1) < this.Theta) {
				this.increasePrice();
			}
			
		}
		else if (this.getInv() > this.getInv_max()){
//			fire one worker
			this.toFire ++;
			
//			consider to decrease price
			if(this.p > this.p_min && Sim.getRnd().getDblFromTo(0, 1) < this.Theta) {
				this.decreasePrice();
			}
			
			
		}
		
	}
	

	// following eq(8) and (9) in main paper
	public void updatePriceRange(){
		this.p_max = (int)(this.phi_max * this.mc);
		this.p_min = (int)(this.phi_min * this.mc);
		
	}
	
	// following eq(10)
	public void increasePrice(){
		this.p = this.p*(1+Sim.getRnd().getDblFromTo(0, this.theta));
		System.out.println(this.toString() + " - increase price:" + this.p);

	}
	public void decreasePrice(){
		this.p = this.p*(1-Sim.getRnd().getDblFromTo(0, this.theta));
		System.out.println(this.toString() + " - decrease price:" + this.p);

	}
	
	

	public int getId() {
		return id;
	}


	public double getW() {
		return w;
	}


	public int getInv() {
		return inv;
	}


	public int getInv_min() {
		return inv_min;
	}


	public int getInv_max() {
		return inv_max;
	}


	public double getP() {
		return p;
	}


	public double getP_min() {
		return p_min;
	}


	public double getP_max() {
		return p_max;
	}
	
//	converts the instance of the class into a string
//	for graphical purposes.
	public String toString(){
		
		return "FI" + this.id;
	}

	public void setD(int d) {
		this.d = d;
	}

	public void setMc(double mc) {
		this.mc = mc;
	}

	public int getOpenPosition() {
		
		return openPosition;
	}
	
	public double getDOpenPosition(){
		return openPosition;
	}

	public void setOpenPosition(int openPosition) {
		this.openPosition = openPosition;
	}

	public int getD() {
		return d;
	}

	public void setInv(int inv) {
		this.inv = inv;
	}

	public double getM() {
		return m;
	}

	public void setM(double m) {
		this.m = m;
	}


	public ArrayList<Integer> getTypeB() {
		return typeB;
	}


	public void setTypeB(ArrayList<Integer> typeB) {
		this.typeB = typeB;
	}


	public ArrayList<Integer> getTypeA() {
		return typeA;
	}


	public void setTypeA(ArrayList<Integer> typeA) {
		this.typeA = typeA;
	}

	public double getM_buffer() {
		return m_buffer;
	}

	public void setM_buffer(double m_buffer) {
		this.m_buffer = m_buffer;
	}

	public int getToFire() {
		if(typeB.size() > 0){
			return toFire;
		}else{
			return 0;
		}
	}

	public void setToFire(int toFire) {
		this.toFire = toFire;
	}

}

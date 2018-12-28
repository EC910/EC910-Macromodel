// This class is built following Lengnick(2013) paper on 
// Journal of Economic Behavior & Organization 86 (2013) 102– 120.

public class Household {
	

//	Id of the object
	private int id;
//	Reservation wage as in pg.106
	private double w;
//	Liquitity as of pg. 106
	private double m;
//	consumption expenditure as in pg. 108
	private int c;
//	labor market connection (typeB) as of pg.105
	private int typeB;
//	good market connections (typeA) as of pg.105
	private int[] typeA;
	
	private double dividend;
	
	
//	employment condition
	public boolean employed; //new 
	
//	consumption parameter
	public double alpha; //new
	
//	average price from connections typeA as of pg.108
	private double P;//new
	
	public Household(int id, double w, double m, int c, int numTypeA, double alpha) {
		this.id = id;
		this.w = w;
		this.m = m;
		this.c = c;
		
		this.typeA = new int[numTypeA];
		
		this.alpha = alpha; //new
	}
	

	public void updateConsumption(){
		
		this.c = (int) Math.min(((double)this.m/this.P)*Math.exp(this.alpha), (double)this.m/this.P);
	}
	
	
	public double getDividend() {
		return dividend;
	}


	public void setDividend(double dividend) {
		this.dividend = dividend;
	}


	public void updateP(double P){
		this.P = P;
	}

	public int getId() {
		return id;
	}

	public double getW() {
		return w;
	}

	public double getM() {
		return m;
	}

	public int getC() {
		return c;
	}
	
//	converts the instance of the class into a string
//	for graphical purposes.
	public String toString(){
		
		return "HH" + this.id;
	}

	public int getTypeB() {
		return typeB;
	}

	public void setTypeB(int typeB) {
		this.typeB = typeB;
	}


	public void setP(double p) {
		P = p;
	}


	public boolean isEmployed() {
		return employed;
	}


	public void setEmployed(boolean employed) {
		this.employed = employed;
	}


	public int[] getTypeA() {
		return typeA;
	}


	public void setTypeA(int[] typeA) {
		this.typeA = typeA;
	}
	
	public void setTypeA(int id, int index) {
		this.typeA[index] = id;
	}


	public void setM(double m) {
		this.m = m;
	}
	

}

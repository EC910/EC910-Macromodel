import jas.engine.Sim;
import jas.engine.SimEngine;
import jas.engine.SimModel;
import jas.engine.gui.JAS;
import jas.events.SimGroupEvent;
import jas.graphics.plot.TimeSeriesPlotter;
import jas.statistics.CrossSection;
import jas.statistics.functions.MaxArrayFunction;
import jas.statistics.functions.MeanArrayFunction;
import jas.statistics.functions.MinArrayFunction;
import jas.statistics.functions.MovingAverageTraceFunction;

public class Observer extends SimModel{

	Model model;

	private CrossSection.Double prices, wages, openpositions, dividends;
	private TimeSeriesPlotter plotter_prices, plotter_wages, plot_employment, plot_production, plot_openpositions,
	plot_dividends;
	
	private int plot_freq = 21;


	@Override
	public void setParameters() {
		// TODO Auto-generated method stub
		model = (Model) Sim.engine.getModelWithID("Model");
		
		Sim.openProbe(this, "Parameters Observer");
	}


	@Override
	public void buildModel() {
		// TODO Auto-generated method stub

//		PRICE PLOT
		prices = new CrossSection.Double(model.getFI_list(), Firm.class, "getP", true);
		
		plotter_prices = new TimeSeriesPlotter("Price");
		plotter_prices.addSeries("avg", new MovingAverageTraceFunction(new MeanArrayFunction(prices), 0, 4));
		plotter_prices.addSeries("max", new MaxArrayFunction.Double(prices));
		plotter_prices.addSeries("min", new MinArrayFunction.Double(prices));
		addSimWindow(plotter_prices);
		
		openpositions = new CrossSection.Double(model.getFI_list(), Firm.class, "getDOpenPosition", true);
		
		plot_openpositions = new TimeSeriesPlotter("OpenPositions");
		plot_openpositions.addSeries("max", new MaxArrayFunction.Double(openpositions));
		plot_openpositions.addSeries("min", new MinArrayFunction.Double(openpositions));
		plot_openpositions.addSeries("avg", new MovingAverageTraceFunction(new MeanArrayFunction(openpositions), 0, 4));
		addSimWindow(plot_openpositions);
		
		dividends = new CrossSection.Double(model.getHH_list(), Household.class, "getDividend", true);
		
		plot_dividends = new TimeSeriesPlotter("Dividend");
		plot_dividends.addSeries("max", new MaxArrayFunction.Double(dividends));
		plot_dividends.addSeries("min", new MinArrayFunction.Double(dividends));
		plot_dividends.addSeries("avg", new MovingAverageTraceFunction(new MeanArrayFunction(dividends), 0, 4));
		addSimWindow(plot_dividends);
		
//		WAGE PLOT
		wages = new CrossSection.Double(model.getFI_list(), Firm.class, "getW", true);
		
		plotter_wages = new TimeSeriesPlotter("Wage");
		plotter_wages.addSeries("avg", new MovingAverageTraceFunction(new MeanArrayFunction(wages), 0, 4));
		plotter_wages.addSeries("max", new MaxArrayFunction.Double(wages));
		plotter_wages.addSeries("min", new MinArrayFunction.Double(wages));
		addSimWindow(plotter_wages);

		plot_production = new TimeSeriesPlotter("Production");
		plot_production.addSeries("total", this.model, "getTotalProduction", true);
		addSimWindow(plot_production);
		
//		EMPLOYMENT
		plot_employment = new TimeSeriesPlotter("Employment Rate");
		plot_employment.addSeries("%", this.model, "getEmployment", true);
		addSimWindow(plot_employment);
		
		this.scheduleEvents();
	}


	private void scheduleEvents() {
		// TODO Auto-generated method stub
		
		SimGroupEvent rate;

		rate = eventList.scheduleGroup(0, this.plot_freq);
		rate.addEvent(plotter_prices, Sim.EVENT_UPDATE);
		rate.addEvent(plotter_wages, Sim.EVENT_UPDATE);
		rate.addEvent(plot_employment, Sim.EVENT_UPDATE);
		rate.addEvent(plot_production, Sim.EVENT_UPDATE);
		rate.addEvent(plot_openpositions, Sim.EVENT_UPDATE);
		rate.addEvent(plot_dividends, Sim.EVENT_UPDATE);
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub

		SimEngine eng = new SimEngine();
		JAS jas = new JAS(eng);
		jas.setVisible(true);

		Model m = new Model();
		eng.addModel(m);
		m.setParameters();

		Observer o = new Observer();
		eng.addModel(o);
		o.setParameters();
	}

}

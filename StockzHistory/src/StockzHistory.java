import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.l33tindustries.tools.file.SystemFile;
import com.l33tindustries.tools.network.wget;

public class StockzHistory
{
	static final String version = "Version 3";
	static final String usage = " USAGE: <STOCK> <# of Days historical>";
	static  String EndString = ";";
	static final String output = " Date,Open,High,Low,Close,Volume,Adj Close";
	
	static String defaultURLOption = "sl1rr5mwhgkje7e8p5e9j1va2ps7";
	
	static ExecutorService ThreadexecSvc;
	
	private static Logger logger = Logger.getLogger(StockzHistory.class.getName());
	
	private static String getCurrentMethodName() 
 	{ 
 		StackTraceElement stackTraceElements[] = (new Throwable()).getStackTrace(); 
 		return 	stackTraceElements[1].toString().replaceFirst(stackTraceElements[1].toString().split("\\.")[0]+"\\.", "");
 	}
	
	public static void main(String[] args) 
	{	
		//PropertyConfigurator.configure("DEBUG");
		
		logger.trace(getCurrentMethodName() + " Entering application.");
		
		logger.debug(getCurrentMethodName() + " Creating thread execution service.");
		ThreadexecSvc = Executors.newCachedThreadPool();
		logger.debug(getCurrentMethodName() + " Thread execution service created.");
		
		if (args[0].equals("version"))
		{
			logger.debug(getCurrentMethodName() + " arg[0] is version. Ouputing Version : " + version);
			System.out.println(version);
		}
		
		else if (args[0].equals("usage"))
		{
			logger.debug(getCurrentMethodName() + " arg[0] is usage. Ouput: " + usage);
			System.out.println(usage);
		}
		
		else if (args[0].equals("output"))
		{
			logger.debug(getCurrentMethodName() + " arg[0] is output. Ouput: " + output);
		    System.out.println(output);
		}
		
		else 
		{			
			logger.debug(getCurrentMethodName() + " Running a thread with input: " + args[0] + " " + args[1]);
			ThreadexecSvc.execute((Runnable) new AnalyzeStock(args[0], args[1]));		

			logger.debug(getCurrentMethodName() + " Shutting down threads.. ");
			ThreadexecSvc.shutdownNow();
			
			logger.trace(getCurrentMethodName() + " Exiting application.");
		}
	}
	
	public static boolean isNull(String str2) {
		logger.trace(getCurrentMethodName() + " Entering.");

        return str2 == null ? true : false;
    }
}

class AnalyzeStock implements Runnable
{
	private String StockSymbol;
	private int numberofdays;
	
	static wget httpGetter = new wget();
	
	AnalyzeStock(String Symbol, String NumberOfDays)
	{
		StockSymbol = Symbol.toUpperCase();
		numberofdays = Integer.parseInt( NumberOfDays );
	}
	private static Logger logger = Logger.getLogger(AnalyzeStock.class.getName());
	
	private static String getCurrentMethodName() 
 	{ 
 		StackTraceElement stackTraceElements[] = (new Throwable()).getStackTrace(); 
 		return 	stackTraceElements[1].toString().replaceFirst(stackTraceElements[1].toString().split("\\.")[0]+"\\.", "");
 	}
	
	public void run()
	{	
		logger.trace(getCurrentMethodName() + " Entering.");
		try 
		{			
			   logger.debug(getCurrentMethodName() + " Creating the calendar instance");
			   Calendar calendar = Calendar.getInstance();
			   logger.debug(getCurrentMethodName() + " Setting SimpleDateFormat MM/dd/yyyy");
			   SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			   
		       String todayDate = dateFormat.format(calendar.getTime()).toString();
		       String tMonth = todayDate.split("/")[0];
		       String tDay = todayDate.split("/")[1];
		       String tYear = todayDate.split("/")[2];
		       
		       int tiMonth = Integer.parseInt( tMonth );
		       
		       int dayback = numberofdays * -1;
		       logger.debug(getCurrentMethodName() + " Going this many days back : " + dayback);

		       calendar.add(Calendar.DAY_OF_MONTH, dayback);
		       
		   String TargetDate = dateFormat.format(calendar.getTime()).toString();
			   
		   String pMonth = TargetDate.split("/")[0];
		   String pDay = TargetDate.split("/")[1];
		   String pYear = TargetDate.split("/")[2];
		       
		   int piMonth = Integer.parseInt( pMonth );
		    
		   int random = (int) (Math.random() * ( 99999 - 0 ));
		        		        
		   String StockFilename = StockSymbol + "_" + random + ".stk";
		   logger.debug(getCurrentMethodName() + " Setting the random file to " + StockFilename);
		       
		   
			//http://ichart.finance.yahoo.com/table.csv?s=YHOO&a=3&b=12&c=1996&d=9&e=1&f=2012&g=d&ignore=.csv
		   logger.debug(getCurrentMethodName() + " Trying : http://ichart.finance.yahoo.com/table.csv?s=" + StockSymbol + "&a=" + (piMonth-1) + "&b=" + pDay + "&c=" + pYear + "&d="+ (tiMonth-1) + "&e=" + tDay + "&f=" + tYear + "&g=d&ignore=.csv");
		   
		   httpGetter.get("http://ichart.finance.yahoo.com/table.csv?s=" + StockSymbol + "&a=" + (piMonth-1) + "&b=" + pDay + "&c=" + pYear + "&d="+ (tiMonth-1) + "&e=" + tDay + "&f=" + tYear + "&g=d&ignore=.csv", StockFilename);	
			
			SystemFile file = new SystemFile(StockFilename);
				 			        
			String STOCK = file.OutputAllLines();

			file.DeleteFile();
					
			String[] lines = STOCK.split(System.getProperty("line.separator"));
			
			for (int i = 0; lines.length > i; i++)
			{
				if (i == 0){ continue;} 
				else
				{
				String line = lines[i].replaceFirst(",", ","+StockSymbol+",");
				line = line.replaceAll("\"", "");
				line = line.replaceAll(" ", "");
				line = line.replaceAll("N\\/A", "0");
			
				String date = line.split(",")[0];
				
				String Year = date.split("-")[0];
				String Month = date.split("-")[1];
				String Day = date.split("-")[2];
				
				String ITM = null;
				String ITMyear = null;
				if (Year.startsWith("19"))
				{
					ITMyear = "0";
				}
				else
				{
					ITMyear = "1";
				}
				
				ITM = ITMyear + Year.substring(2,4) + Month + Day + "000000000";
				
				line = line.replaceFirst(date, ITM);
				
				System.out.println(StockSymbol + "-" + ITM + "," + line + "," + date);
				
				}				
			}

		} 
	
		catch (IOException e) 
		{
			System.out.println("Could not reach the internet to get the stock prices for " + StockSymbol);
		}
	}
}

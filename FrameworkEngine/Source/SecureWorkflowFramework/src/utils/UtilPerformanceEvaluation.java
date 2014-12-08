package utils;

import java.io.FileWriter;
/**
 * This class is used to write the performance data into csv files.
 * @author Sardar Hussain
 *
 */
public class UtilPerformanceEvaluation {
	
	public UtilPerformanceEvaluation()
	{
		
	}
	/**
	 * This function write the columns of the excel file.
	 * @param _columns The columns i.e. comma separated column names
	 */
	public static void recordColumns(String _columns)
	{
		String filename= System.getProperty("user.dir") + "//PerformanceEvaluation//performance.csv";
		try{
			
			FileWriter fw = new FileWriter(filename,true);
			fw.write(_columns);
			fw.write(System.getProperty("line.separator"));
			fw.close();
		}catch(Exception ex)
		{
			System.out.println("Performance:" + ex.getMessage());
		}
	}
	/**
	 * This function write one entry i.e. the processing time.
	 * @param _data The column values
	 */
	public static void recordEntry(String _data)
	{
		String filename= System.getProperty("user.dir") + "//PerformanceEvaluation//performance.csv";
		try{
			
			FileWriter fw = new FileWriter(filename,true);
			fw.write(_data);
			fw.write(System.getProperty("line.separator"));
			fw.close();
		}catch(Exception ex)
		{
			System.out.println("Performance:" + ex.getMessage());
		}
	}
	public static void recordAnalyzerEntry(String _file,String _data)
	{
		try{
			
			FileWriter fw = new FileWriter(_file,true);
			fw.write(_data);
			fw.write(System.getProperty("line.separator"));
			fw.close();
		}catch(Exception ex)
		{
			System.out.println("Performance:" + ex.getMessage());
		}
	}
	public static void recordEntry2(String _data)
	{
		String filename= System.getProperty("user.dir") + "//PerformanceEvaluation//performance2.csv";
		try{
			
			FileWriter fw = new FileWriter(filename,true);
			fw.write(_data);
			fw.write(System.getProperty("line.separator"));
			fw.close();
		}catch(Exception ex)
		{
			System.out.println("Performance:" + ex.getMessage());
		}
	}
}

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

public class miniSqlParser {
	
	//static HashMap<String, HashSet<String>> tablesCreated = new HashMap<String, HashSet<String>>();
	static HashMap<String, Vector<String>> tablesCreated = new HashMap<String, Vector<String>>();
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws NumberFormatException, IOException {
		
		//Scanner to read the query
		Scanner in = new Scanner(System.in);
		try 
		{
			init();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
			
		while(true)
		{
			System.out.print("mysql>");
			String query = in.nextLine();
			if(query.length() < 1)
				continue;
			Parser P = new Parser();
			try 
			{
				P.parseQuery(query);
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("resource")
	public static void init() throws IOException
	{
		File metaFile = new File("src/metadata.txt");
		if(metaFile.exists())
		{
			// If the file exists then load the file into the hashmap for checking the table columns
					
			BufferedReader metaFileReader = new BufferedReader(new FileReader(metaFile));                                                 
		
			String lineInMetaFile, tableName = "";
						
			while((lineInMetaFile = metaFileReader.readLine( )) != null) 
			{
				//Check if the line is <begin_table>, it interperts the start the table 
				if(lineInMetaFile.contains("<begin_table>"))
				{
					//Get the set 
//					HashSet<String> columns = new HashSet<String>();
					Vector<String > columns = new Vector<String>();
					
					// Get the table name
					tableName = metaFileReader.readLine( );
										
					//Read the file till the end of the table i.e. end tag "<end_table>"
					while(!(lineInMetaFile = metaFileReader.readLine( )).equals("<end_table>"))
					{
						columns.add(lineInMetaFile);
						//columns. add(lineInMetaFile);
					}
					tablesCreated.put(tableName, columns);
				}
			}
		}
	}
	
	public static void writeToMetaFile() throws IOException
	{
		FileWriter metaWrite = new FileWriter("src/metadata.txt", false);
				
		Set<String> keySet = tablesCreated.keySet();
		Iterator<String> keySetIterator = keySet.iterator();
		
		while(keySetIterator.hasNext()) 
		{
		   String key = keySetIterator.next();
		   metaWrite.write("<begin_table>\n");
		   metaWrite.write(key + "\n");
		   
		   for(int i = 0; i < tablesCreated.get(key).size(); i++)
			   metaWrite.write(tablesCreated.get(key).get(i)+"\n");
		   
		   if(keySetIterator.hasNext() == false)
			   metaWrite.write("<end_table>");
		   else
			   metaWrite.write("<end_table>\n");
		}
		
		metaWrite.close();
	}
}
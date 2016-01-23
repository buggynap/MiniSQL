import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



class Parser {
	
	public void parseQuery(String query) throws NumberFormatException, IOException
	{
		String firstWordTokens[] = query.toLowerCase().split(" ");
		switch(firstWordTokens[0])
		{
			case "select":
				try 
				{
					parseSelectStatement(query);
				}
				catch (FileNotFoundException e) 
				{
					e.printStackTrace();
				}
				break;
			case "create":
				parseCreate(query);
				break;
			case "insert":
				parseInsert(query);
				break;
			case "truncate":
				try 
				{
					parseTruncate(query);
				}
				catch (FileNotFoundException e) 
				{
					e.printStackTrace();
				}
				break;
			case "delete":
				parseDelete(query);
				break;
			case "drop":
				try 
				{
					parseDrop(query);
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
				break;
			case "exit":
				System.out.println("Bye...");
				System.exit(0);
				break;
			default:
				errorRoutine(query);
				break;				
		}
	}
	
	public void parseSelectStatement(String queryText) throws NumberFormatException, IOException
	{
		//Disect the select the query and then perform the query operation 
		
		//Get the text between select word and from word
		
		int startIndex = 5, endIndex = queryText.toLowerCase().indexOf("from ");
		
		//Check if "from" keyword exists or not
		if(endIndex == - 1)
		{
			errorRoutine(queryText);
			return;
		}		

		ArrayList<String> columns = new ArrayList<String>();
		
		StringBuilder tempColumn = new StringBuilder();

		if(queryText.charAt(startIndex + 1) != ' ' || queryText.charAt(endIndex - 1) != ' ') 
		{
			// If the query is like select* from emp OR select *from emp--> throw error
			errorRoutine(queryText);
			return;
		}
		else
		{
			for(int i = startIndex + 2; i < endIndex; i++)
			{
				if(queryText.charAt(i) == ',') 
				{
					if(i == startIndex)
					{
						// If the query is like select ,* from emp --> throw error
						errorRoutine(queryText);
						return;
					}
					// else add to the columns list
					columns.add(tempColumn.toString());
					tempColumn.setLength(0);
				}
				else if(queryText.charAt(i) != ' ')
				{
					tempColumn.append(queryText.charAt(i));
				}
				if(i == endIndex - 1)
				{
					columns.add(tempColumn.toString());
					tempColumn.setLength(0);
				}
			}			
		}
		
		int noOfColumns = columns.size();
		boolean conditionPresent = false;
		
		// Get the table names may be till the end of the query or till the end of the where clause
		// check for it
				
		startIndex = endIndex + 5;
		
		endIndex = queryText.toLowerCase().indexOf("where");
		
		if(endIndex > -1)
			conditionPresent = true;

		endIndex = (endIndex == -1? queryText.length() : endIndex);
		
		ArrayList<String> tableNames = new ArrayList<String>();
		
		for(int i = startIndex; i < endIndex; i++)
		{
			if(queryText.charAt(i) == ',')
			{
				if(i == startIndex)
				{
					errorRoutine(queryText);
					return;
				}
				tableNames.add(tempColumn.toString());
				tempColumn.setLength(0);
			}
			else if(queryText.charAt(i) != ' ')
			{
				tempColumn.append(queryText.charAt(i));
			}
			if(i == endIndex - 1)
			{
				tableNames.add(tempColumn.toString());
				tempColumn.setLength(0);
			}
		}
		
		int andIndex = 0, orIndex = 0;
		boolean andCondition = false;
		boolean orCondition = false;
		StringBuilder conditionColumn1 = new StringBuilder();
		StringBuilder condition1 = new StringBuilder();
		StringBuilder conditionColumnValue1 = new StringBuilder();
		boolean lessThanEqTo1 = false, greaterThanEqTo1 = false, equalsTo1 =  false, greaterThan1 = false, lessThan1 = false;
		boolean lessThanEqTo2 = false, greaterThanEqTo2 = false, equalsTo2 =  false, greaterThan2 = false, lessThan2 = false;
		StringBuilder conditionColumn2 = new StringBuilder();
		StringBuilder condition2 = new StringBuilder();
		StringBuilder conditionColumnValue2 = new StringBuilder();
		
		// If where is present that means where is present parse it till the end of the query
		
		if(endIndex < queryText.length()) // means "where" condition was present in the last condition, hence where location must be less than that of the lenght of the string
		{
			if((andIndex = queryText.toLowerCase().indexOf(" and ")) != -1)
				andCondition = true;
			else if((orIndex = queryText.toLowerCase().indexOf(" or ")) != -1)
				orCondition = true;
		
			startIndex = endIndex + 6;
					
			int i = startIndex;
					
			// Get the index of "and" or "or" and loop till it
			// get condition Column 1
			
			while( i < queryText.length() &&
					queryText.charAt(i) != '<' &&
					queryText.charAt(i) != '>' &&
					queryText.charAt(i) != '=' &&
					queryText.charAt(i) != ' ' )
			{
				conditionColumn1.append(queryText.charAt(i));
				i++;
			}
			
			//loop till nonspace character if it is there
			if(i < queryText.length() && queryText.charAt(i) == ' ')
				while(queryText.charAt(++i) == ' ');
			
			//Get the condition
			
			while(i < queryText.length() && queryText.charAt(i) != ' ' )
			{
				if(queryText.charAt(i) == '=')
				{
					condition1.append(queryText.charAt(i));
					i++;
					break;
				}
				else if(queryText.charAt(i) == '>' || queryText.charAt(i) == '<')
				{
					condition1.append(queryText.charAt(i));
				}
				else
				{
					break;
				}
				i++;
			}
			
			// < and > are not implemented yet
			
			switch(condition1.toString())
			{
				case "=":
					equalsTo1 = true;
					break;
				case "<=":
					lessThanEqTo1 = true;
					break;
				case ">=":
					greaterThanEqTo1 = true;
					break;
				case ">":
					greaterThan1 = true;
					break;
				case "<":
					lessThan1 = true;
					break;
				default:
					errorRoutine(queryText);
					return;				
			}	
						
			//loop till nonspace character if it is there
			if(queryText.charAt(i) == ' ')
				while(queryText.charAt(++i) == ' ');
			
			while( i < queryText.length() &&
					queryText.charAt(i) != ' ')
			{
				conditionColumnValue1.append(queryText.charAt(i));
				i++;
			}
			
			//Only get the next values if and or or condition is there else don't
			if(andCondition)
				i = andIndex + 5;
			else if(orCondition)
				i = orIndex + 4;
			
			if(andCondition || orCondition)
			{
				//loop till nonspace character if it is there
				if(queryText.charAt(i) == ' ')
					while(queryText.charAt(++i) == ' ');
				

				while( i < queryText.length() &&
						queryText.charAt(i) != '<' &&
						queryText.charAt(i) != '>' &&
						queryText.charAt(i) != '=' &&
						queryText.charAt(i) != ' ' )
				{
					conditionColumn2.append(queryText.charAt(i));
					i++;
				}
				
				//loop till nonspace character if it is there
				try
				{
				if(queryText.charAt(i) == ' ')
					while(queryText.charAt(++i) == ' ');
				}
				catch(Exception e)
				{
					errorRoutine(queryText);
					return;
				}
						
				//Get the condition
				while(i < queryText.length() && queryText.charAt(i) != ' ' )
				{
					if(queryText.charAt(i) == '=')
					{
						condition2.append(queryText.charAt(i));
						i++;
						break;
					}
					else if(queryText.charAt(i) == '>' || queryText.charAt(i) == '<')
					{
						condition2.append(queryText.charAt(i));
					}
					else
						break;
					i++;
				}		
				
				switch(condition2.toString())
				{
					case "=":
						equalsTo2 = true;
						break;
					case "<=":
						lessThanEqTo2 = true;
						break;
					case ">=":
						greaterThanEqTo2 = true;
						break;
					case ">":
						greaterThan2 = true;
						break;
					case "<":
						greaterThan2 = true;
						break;
					default:
						errorRoutine(queryText);
						return;				
				}	
				
				if(queryText.charAt(i) == ' ')
					while(queryText.charAt(++i) == ' ');
				
				while( 	i < queryText.length() &&
						queryText.charAt(i) != ' ')
				{
					conditionColumnValue2.append(queryText.charAt(i));
					i++;
				}
			}
		}
		/*
		System.out.println("No Of Columns in Select Query : " + noOfColumns);
		for(int i = 0; i < tableNames.size(); i++)
			System.out.println("Table : " + tableNames.get(i));
		
		for(int i = 0; i < columns.size(); i++)
			System.out.println("Rows : " + columns.get(i));
		
		System.out.println(	"AND Condition : " + andCondition +
							"\nOR Condition : " + orCondition + 
							"\n Condition Column 1 : " + conditionColumn1 + 
							"\n Condition 1 : " + condition1  + 
							"\n Condition Column Value 1 : " + conditionColumnValue1 +
							"\n Condition columns value 2 : " + conditionColumn2 + 
							"\n Condition 2 : " + condition2 +
							"\n Condition Column Value 2 " + conditionColumnValue2 +
							"\n Is less than in first Condition 1: " + lessThan1 +
							"\n Is greater than in first Condition 1 : " + greaterThan1+
							"\n Is greater than eq in first Condition 1 : " + greaterThanEqTo1+
							"\n Is less than eq in first Condition 1 : " + lessThanEqTo1+
							"\n Is equals to in first Condition 1: " +  equalsTo1 +
							"\n Is less than in first Condition 1: " + lessThan2 +
							"\n Is greater than in first Condition 1 : " + greaterThan2 +
							"\n Is greater than eq in first Condition 1 : " + greaterThanEqTo2 +
							"\n Is less than eq in first Condition 1 : " + lessThanEqTo2 +
							"\n Is equals to in first Condition 1: " +  equalsTo2  
							);*/
		
		// First lets check for the * in the parsed select query
		
		if(columns.size() == 1)  // means single column is present
		{
			//Check if the column is * or not
			if(columns.get(0).toString().equals("*"))
			{
				if(conditionPresent)
				{
					if(andCondition == false && orCondition == false)
					{
						if(tableNames.size() == 1)
						{
							//Check whether the table is present or not
							if(!isTablePresent(tableNames.get(0)))
							{
								System.out.println("\"" + tableNames.get(0) + "\" is not present...");
								return;
							}
							
							//Check whether the the column is present or not in the table 
							
							for(int i = 0; i < columns.size(); i++)
							{
								if(!isColumnPresentInTable(tableNames.get(0), conditionColumn1.toString()))
								{
									System.out.println("Column is not part of the table...");
									return;
								}	
							}
							
							Vector <String> colResult = new Vector <String>();
							
							int index = getIndex(tableNames.get(0), conditionColumn1.toString());
	
							BufferedReader br = new BufferedReader(new FileReader("src/"+tableNames.get(0)+".csv"));
							String line;
							try 
							{
								while((line = br.readLine()) != null)
								{
									String colList[] = line.split(",");
									
									if(equalsTo1)
									{
										if(colList[index].trim().equals(conditionColumnValue1.toString().trim()))
											colResult.add(line);
									}
									else if(greaterThan1)
									{
										if(Float.parseFloat(colList[index].trim()) > Float.parseFloat(conditionColumnValue1.toString().trim()))
											colResult.add(line);
									}
									else if(lessThan1)
									{
										if(Float.parseFloat(colList[index].trim()) < Float.parseFloat(conditionColumnValue1.toString().trim()))
											colResult.add(line);
									}
									else if(lessThanEqTo1)
									{
										if(Float.parseFloat(colList[index].trim()) <= Float.parseFloat(conditionColumnValue1.toString().trim()))
											colResult.add(line);
									}
									else if(greaterThanEqTo1)
									{
										if(Float.parseFloat(colList[index].trim()) >= Float.parseFloat(conditionColumnValue1.toString().trim()))
											colResult.add(line);
									}
									else
									{
										errorRoutine(queryText);
										return;
									}
								}
							} 
							catch (NumberFormatException e) 
							{
								e.printStackTrace();
							} 
							catch (IOException e) 
							{
								e.printStackTrace();
							}
							
							//Display the result
							
							if(columns.get(0).equals("*"))
							{
								for(int i = 0; i < miniSqlParser.tablesCreated.get(tableNames.get(0)).size(); i++)
								{
									System.out.print(miniSqlParser.tablesCreated.get(tableNames.get(0)).get(i) + "\t");
								}
								System.out.println();
								
								for(int i = 0; i < colResult.size(); i++)
								{
									String splittedRows[] = colResult.get(i).toString().split(",");
									for(int j = 0; j < splittedRows.length; j++)
										System.out.print(splittedRows[j]+"\t");
									System.out.println();
								}
							}
							else
							{
								//Print the column list
								for(int j = 0; j < columns.size(); j++)
									System.out.print(columns.get(j)+"\t");
								System.out.println();
								//for that may be I will get the display column list
								
								for(int k = 0; k < colResult.size(); k++)
								{
									String splitRow[] = colResult.get(k).split(",");
									
									for(int i = 0; i < miniSqlParser.tablesCreated.get(tableNames.get(0)).size(); i++)
									{
										for(int j = 0; j < columns.size(); j++)
										{
											if(getIndex(tableNames.get(0), miniSqlParser.tablesCreated.get(tableNames.get(0)).get(i))
													== getIndex(tableNames.get(0), columns.get(j)))
											{
												System.out.print(splitRow[getIndex(tableNames.get(0), columns.get(j))]+ "\t" );
												
											}		
										}
									}
									System.out.println();
								}
							}
						}
					}
					else// means a condition is true check for that condition
					{		
						// why not to first check for the table to which the condition column they belong to
						//Check if tables exists or not
						
						for(int i = 0; i < tableNames.size(); i++)
						{
							if(!isTablePresent(tableNames.get(i)))
							{
								System.out.println("\"" + tableNames.get(i) + "\" does not exists...");
								return;
							}
						}
						
						//This variable is for the getting the table to which the first column belongs to in the condition
						String fstTable = null;
						
						//Check of the table to which fst column belongs if it does not belong to any table throw error
						//Iterate over the tablelist
						
						int flag = 0;
						for(int i = 0; i < tableNames.size(); i++)
						{
							if(isColumnPresentInTable(tableNames.get(i),conditionColumn1.toString()))
							{
								fstTable = tableNames.get(i);
								flag = 1;
								break;
							}
						}
						
						if(flag == 0)
						{
							System.out.println("\"" + conditionColumn1 + "\" column is not part of any table");
							return;
						}
						
						
						//This variable is for the getting the table to which the sec column belongs to in the condition
						String secTable = null;
						
						//Check of the table to which sec column belongs if it does not belong to any table throw error
						//Iterate over the tablelist
						
						flag = 0;
						for(int i = 0; i < tableNames.size(); i++)
						{
							if(isColumnPresentInTable(tableNames.get(i),conditionColumn2.toString()))
							{
								secTable = tableNames.get(i);
								flag = 1;
								break;
							}
						}
						
						if(flag == 0)
						{
							System.out.println("\"" + conditionColumn2 + "\" column is not part of any table");
							return;
						}
						
						Vector <String> colResult = new Vector <String>();
						// If "or" condition then no need to check anything just add the columns in the result vector
						//get the rows from the first table
						BufferedReader br = new BufferedReader(new FileReader("src/" + fstTable + ".csv"));
						
						int index = getIndex(fstTable, conditionColumn1.toString());

						String line;
						try 
						{
							while((line = br.readLine()) != null)
							{
								String colList[] = line.split(",");
								
								if(equalsTo1)
								{
									if(colList[index].trim().equals(conditionColumnValue1.toString().trim()))
										colResult.add(line);
								}
								else if(greaterThan1)
								{
									if(Float.parseFloat(colList[index].trim()) > Float.parseFloat(conditionColumnValue1.toString().trim()))
										colResult.add(line);
								}
								else if(lessThan1)
								{
									if(Float.parseFloat(colList[index].trim()) < Float.parseFloat(conditionColumnValue1.toString().trim()))
										colResult.add(line);
								}
								else if(lessThanEqTo1)
								{
									if(Float.parseFloat(colList[index].trim()) <= Float.parseFloat(conditionColumnValue1.toString().trim()))
										colResult.add(line);
								}
								else if(greaterThanEqTo1)
								{
									if(Float.parseFloat(colList[index].trim()) >= Float.parseFloat(conditionColumnValue1.toString().trim()))
										colResult.add(line);
								}
								else
								{
									errorRoutine(queryText);
									return;
								}
							}
						} 
						catch (NumberFormatException e) 
						{
							e.printStackTrace();
						} 
						catch (IOException e) 
						{
							e.printStackTrace();
						}
						finally
						{
							br.close();
						}
						
						if(!fstTable.equals(secTable) && !orCondition)
						{
							System.out.println("###"+fstTable+"###"+secTable+"###");
							
							//Get the rows from the second table
							br = new BufferedReader(new FileReader("src/" + secTable + ".csv"));
							
							index = getIndex(secTable, conditionColumn2.toString());
	
							try 
							{
								while((line = br.readLine()) != null)
								{
									String colList[] = line.split(",");
									
									if(equalsTo2)
									{
										if(colList[index].trim().equals(conditionColumnValue2.toString().trim()) && !colResult.contains(line))
											colResult.add(line);
									}
									else if(greaterThan2)
									{
										if(Float.parseFloat(colList[index].trim()) > Float.parseFloat(conditionColumnValue2.toString().trim()) && !colResult.contains(line))
											colResult.add(line);
									}
									else if(lessThan2)
									{
										if(Float.parseFloat(colList[index].trim()) < Float.parseFloat(conditionColumnValue2.toString().trim()) && !colResult.contains(line))
											colResult.add(line);
									}
									else if(lessThanEqTo2)
									{
										if(Float.parseFloat(colList[index].trim()) <= Float.parseFloat(conditionColumnValue2.toString().trim()) && !colResult.contains(line))
											colResult.add(line);
									}
									else if(greaterThanEqTo2)
									{
										if(Float.parseFloat(colList[index].trim()) >= Float.parseFloat(conditionColumnValue2.toString().trim()) && !colResult.contains(line))
											colResult.add(line);
									}
									else
									{
										errorRoutine(queryText);
										return;
									}
								}
							} 
							catch (NumberFormatException e) 
							{
								e.printStackTrace();
							} 
							catch (IOException e) 
							{
								e.printStackTrace();
							}
							finally
							{
								br.close();
							}
						}
						
						if(orCondition || !fstTable.equals(secTable))
						{
							//print the result as per the columns asked by the user
							for(int i = 0; i < colResult.size(); i++)
							{
								String csv[] = colResult.get(i).toString().split(",");
								for(int j = 0; j < csv.length; j++)
								{
									System.out.print(csv[j] + "\t");
								}
								System.out.println();
							}
						}
						else 
						{
							//	means the table to which the first condition column belong is same as that of the second condition column
							// 	Both are same
							//Need to check both the conditions
							
							int indexOfColumn = getIndex(secTable, conditionColumn2.toString());
							Vector <String> result = new Vector <String>();
							
							for(int i = 0; i < colResult.size(); i++)
							{
								String csv[] = colResult.get(i).toString().split(",");
								for(int j = 0; j < csv.length; j++)
								{
									//Check for the both the conditions at the same time
									if(equalsTo2)
									{
										if(csv[indexOfColumn].trim().equals(conditionColumnValue2.toString().trim()) && !result.contains(colResult.get(i)))
											result.add(colResult.get(i));
									}
									else if(greaterThan2)
									{
										if(Float.parseFloat(csv[indexOfColumn].trim()) > Float.parseFloat(conditionColumnValue2.toString().trim()) && !result.contains(colResult.get(i)))
											result.add(colResult.get(i));
									}
									else if(lessThan2)
									{
										if(Float.parseFloat(csv[indexOfColumn].trim()) < Float.parseFloat(conditionColumnValue2.toString().trim()) && !result.contains(colResult.get(i)))
											result.add(colResult.get(i));
									}
									else if(lessThanEqTo2)
									{
										if(Float.parseFloat(csv[indexOfColumn].trim()) <= Float.parseFloat(conditionColumnValue2.toString().trim()) && !result.contains(colResult.get(i)))
											result.add(colResult.get(i));
									}
									else if(greaterThanEqTo2)
									{
										if(Float.parseFloat(csv[indexOfColumn].trim()) >= Float.parseFloat(conditionColumnValue2.toString().trim()) && !result.contains(colResult.get(i)))
											result.add(colResult.get(i));
									}
									else
									{
										errorRoutine(queryText);
										return;
									}	
								}
							}
							for(int i = 0; i < result.size(); i++)
							{
								String csv[] = result.get(i).toString().split(",");
								for(int j = 0; j < csv.length; j++)
								{
									System.out.print(csv[j] + "\t");
								}
								System.out.println();
							}
						}
					}
				}
				else
				{
			
					Vector<String> result = new Vector<String>();
					
					// This is required if the query contains more than one table
				
					for(int i = 0; i< tableNames.size(); i++)
					{
						String tableName = "src/" + tableNames.get(i).toString() + ".csv";
						File f = new File(tableName);
						
						Vector<String> tempResult = new Vector<String>();
						
						if(f.exists())
						{
							// 	First get all the rows 
							BufferedReader tableFileReader = new BufferedReader(new FileReader(f));
							String rowInTable;
							try 
							{
								while((rowInTable = tableFileReader.readLine()) != null) 
								{
									tempResult.add(rowInTable);
								}
							}
							catch (IOException e) 
							{				
								e.printStackTrace();
							}
							
							//Merge with the previous result
						
							// If first time then directly copy the result in the result vector
							if(i == 0)
							{
								result = (Vector<String>) tempResult.clone();
							}
							else
							{
								Vector<String> temp = new Vector<String>();
								int j, k;
								for(j = 0; j < result.size(); j++)
								{
									String tempCol = result.get(j);
									for(k = 0; k < tempResult.size(); k++)
									{
										temp.add(j + k, tempCol + "," + tempResult.get(k));
									}
								}
								result.clear();
								tempResult.clear();
								result = (Vector<String>) temp.clone();
								temp.clear();
							}
						}
						else
						{
							System.out.println("Table does not exists!!!");
							return;
						}
					}
					
					for(int i = 0; i < tableNames.size(); i++)
					{
						for(int j = 0; j < miniSqlParser.tablesCreated.get(tableNames.get(i)).size(); j++)
						{
							System.out.print(miniSqlParser.tablesCreated.get(tableNames.get(i)).get(j)+"\t");
						}	
					}	
					System.out.println();
					
					//Show the result
					if(tableNames.size() > 1)
						Collections.reverse(result);
					
					for(int j = 0; j < result.size(); j++)
					{
						String temp[] = result.get(j).split(",");
						for(int k = 0; k < temp.length; k++)
							System.out.print(temp[k] + "\t");
						System.out.print("\n");
					}
				}
			}
			else
			{
				//Check if the column name is in the table list or not or check if the column is any aggreagate function or not
				// like if the user enters the query "select colName from table1, table2;
			
				String currentQueryName = columns.get(0).trim();
				
				if(currentQueryName.matches(".*\\(.*\\)"))
				{
					StringBuilder aggrFunctionName = new StringBuilder();
					StringBuilder currentColName = new StringBuilder();
					int i;
				
					//To get the name of the aggregate function
					for(i = 0; i < currentQueryName.length(); i++)
					{
						if(currentQueryName.charAt(i) == '(')
						{
							break;
						} 
						else if(currentQueryName.charAt(i) != ' ')
						{
							aggrFunctionName.append(currentQueryName.charAt(i)); 
						}
					}
				
					//To get the name of the column on which it is performed 
					//AND YES REMEMBER CAN BE ON * TOO
					for(i = i + 1; i < currentQueryName.length(); i++) 
					{
						if(currentQueryName.charAt(i) == ')')
						{
							break;
						}
						else if(currentQueryName.charAt(i) != ' ') 
						{
							currentColName.append(currentQueryName.charAt(i)); 
						}
					}
					
					switch(aggrFunctionName.toString().toLowerCase())
					{
						case "sum":
							if(tableNames.size() > 1)
								System.out.println("More than one table isn't supported...");
							else
							{
								float sum = getSumAggr(currentColName.toString(), tableNames.get(0).toString());
								if(sum ==  -1)
									return;
								System.out.println(aggrFunctionName+"("+currentColName+")\n  "+sum);								
							}
							break;
						case "avg":
							if(tableNames.size() > 1)
								System.out.println("More than one table isn't supported...");
							else
							{
								float avg = getAvgAggr(currentColName.toString(), tableNames.get(0).toString());
								if(avg ==  -1)
									return;
								System.out.println(aggrFunctionName+"("+currentColName+")\n  "+avg);								
							}
							break;
						case "max":
							if(tableNames.size() > 1)
								System.out.println("More than one table isn't supported...");
							else
							{
								float max = getMaxAggr(currentColName.toString(), tableNames.get(0).toString());
								if(max ==  -1)
									return;
								System.out.println(aggrFunctionName+"("+currentColName+")\n  " + max);								
							}
							break;
						case "min":
							if(tableNames.size() > 1)
								System.out.println("More than one table isn't supported...");
							else
							{
								float min = getMinAggr(currentColName.toString(), tableNames.get(0).toString());
								if(min ==  -1)
									return;
								System.out.println(aggrFunctionName+"("+currentColName+")\n  " + min);								
							}
							break;
						case "distinct":
							if(tableNames.size() > 1)
								System.out.println("More than one table isn't supported...");
							else
							{
								int retValue = getDistinctRows(currentColName.toString(), tableNames.get(0).toString());
								if(retValue == -1)
									return;										
							}
							break;
						default :
							errorRoutine(queryText);
							return;
					}					
				}
				else // it can be single column name run the loop for it
				{
					if(conditionPresent)
					{
						if(tableNames.size() == 1)
						{
							//Check whether the table is present or not
							if(!isTablePresent(tableNames.get(0)))
							{
								System.out.println("\"" + tableNames.get(0) + "\" is not present...");
								return;
							}
							
							//Check whether the the column is present or not in the table 
							
							for(int i = 0; i < columns.size(); i++)
							{
								if(!isColumnPresentInTable(tableNames.get(0), columns.get(i)))
								{
									System.out.println("Column is not part of the table...");
								}	
							}
							
							Vector <String> colResult = new Vector <String>();
							
							int index = getIndex(tableNames.get(0), conditionColumn1.toString());

							BufferedReader br = new BufferedReader(new FileReader("src/"+tableNames.get(0)+".csv"));
							String line;
							try 
							{
								while((line = br.readLine()) != null)
								{
									String colList[] = line.split(",");
									
									if(equalsTo1)
									{
										if(colList[index].trim().equals(conditionColumnValue1.toString().trim()))
											colResult.add(line);
									}
									else if(greaterThan1)
									{
										if(Float.parseFloat(colList[index].trim()) > Float.parseFloat(conditionColumnValue1.toString().trim()))
											colResult.add(line);
									}
									else if(lessThan1)
									{
										if(Float.parseFloat(colList[index].trim()) < Float.parseFloat(conditionColumnValue1.toString().trim()))
											colResult.add(line);
									}
									else if(lessThanEqTo1)
									{
										if(Float.parseFloat(colList[index].trim()) <= Float.parseFloat(conditionColumnValue1.toString().trim()))
											colResult.add(line);
									}
									else if(greaterThanEqTo1)
									{
										if(Float.parseFloat(colList[index].trim()) >= Float.parseFloat(conditionColumnValue1.toString().trim()))
											colResult.add(line);
									}
									else
									{
										errorRoutine(queryText);
										return;
									}
								}
							} 
							catch (NumberFormatException e) 
							{
								e.printStackTrace();
							} 
							catch (IOException e) 
							{
								e.printStackTrace();
							}
							
							//Display the result
							
							//Print the column list
							for(int j = 0; j < columns.size(); j++)
								System.out.print(columns.get(j)+"\t");
							System.out.println();
							
							//for that may be I will get the display column list
							
							for(int k = 0; k < colResult.size(); k++)
							{
								String splitRow[] = colResult.get(k).split(",");
								
								for(int i = 0; i < miniSqlParser.tablesCreated.get(tableNames.get(0)).size(); i++)
								{
									for(int j = 0; j < columns.size(); j++)
									{
										if(getIndex(tableNames.get(0), miniSqlParser.tablesCreated.get(tableNames.get(0)).get(i))
												== getIndex(tableNames.get(0), columns.get(j)))
										{
											System.out.print(splitRow[getIndex(tableNames.get(0), columns.get(j))]+ "\t" );
											
										}		
									}
								}
								System.out.println();
							}
						}
					}
					else
					{
					
						// First check for the tables whether they are present or not			
						for(int i = 0; i < tableNames.size(); i++) 
						{
							if(!isTablePresent(tableNames.get(i)))
							{
								System.out.println("\"" +tableNames.get(i)+"\" Table does not exists.");
								return;
							}
						}
						
						//	Then check for the column whether they are present in one of the table or not
						// again we can check for the ambigius column name in the table
						for(int i = 0; i < tableNames.size(); i++) 
						{
							int ctr = 0;
							for(int j = 0; j < columns.size(); j++) 
							{
								if(isColumnPresentInTable(tableNames.get(i), columns.get(j)))
								{
									ctr++;
								}
								else
								{
									System.out.println("\"" +columns.get(i)+"\" column does not exists.");
									return;
								}
							}
							if(ctr == 2)
								System.out.println("One or more ambigiuous column in tables...");
						}
					
						// 	Now get the index of the columns from the tablecreated hashmap and loop over the tables i.e. csv file
					

						for(int i = 0; i < columns.size(); i++)
							System.out.print(columns.get(i)+"\t");
						System.out.println();
					
						Vector<String> result = new Vector<String>();
					
						for(int i = 0; i < columns.size(); i++)
						{
							for(int j = 0; j < tableNames.size(); j++)
							{
								if(isColumnPresentInTable(tableNames.get(j), columns.get(i)))
								{
									int colIndex = getIndex(tableNames.get(j), columns.get(i));
								
									// Now read the file of the specific index after the tokenization
								
									String tableName = "src/" + tableNames.get(j).toString() + ".csv";
									File f = new File(tableName);
												
									if(f.exists())
									{
										// First get all the rows 
										BufferedReader tableFileReader = new BufferedReader(new FileReader(f));
										String rowInTable;
										try 
										{
											while((rowInTable = tableFileReader.readLine()) != null) 
											{
												String fields[] = rowInTable.split(",");
												result.add(fields[colIndex]);
											}
										}
										catch (IOException e) 
										{				
											e.printStackTrace();
										}						
									}
								}				
							}
						}
						for (String string : result) 
						{
							String outputColumns[] = string.split(",");
							for(int i = 0; i < outputColumns.length; i++)
							{
								System.out.print(outputColumns[i]+"\t");
							}
							System.out.println();
						}
					}
				}
			}
		}
		else  //More than one column
		{
			if(conditionPresent)
			{
				if(tableNames.size() == 1)
				{
					//Check whether the table is present or not
					if(!isTablePresent(tableNames.get(0)))
					{
						System.out.println("\"" + tableNames.get(0) + "\" is not present...");
						return;
					}
					
					//Check whether the the column is present or not in the table 
					
					for(int i = 0; i < columns.size(); i++)
					{
						if(!isColumnPresentInTable(tableNames.get(0), columns.get(i)))
						{
							System.out.println("Column is not part of the table...");
							return;
						}	
					}
					
					//	Also check for the condition column  
					
					for(int i = 0; i < columns.size(); i++)
					{
						if(!isColumnPresentInTable(tableNames.get(0), conditionColumn1.toString()))
						{
							System.out.println("Column is not part of the table...");
							return;
						}	
					}
					
					Vector <String> colResult = new Vector <String>();
					
					int index = getIndex(tableNames.get(0), conditionColumn1.toString());

					BufferedReader br = new BufferedReader(new FileReader("src/"+tableNames.get(0)+".csv"));
					String line;
					try 
					{
						while((line = br.readLine()) != null)
						{
							String colList[] = line.split(",");
							
							if(equalsTo1)
							{
								if(colList[index].trim().equals(conditionColumnValue1.toString().trim()))
									colResult.add(line);
							}
							else if(greaterThan1)
							{
								if(Float.parseFloat(colList[index].trim()) > Float.parseFloat(conditionColumnValue1.toString().trim()))
									colResult.add(line);
							}
							else if(lessThan1)
							{
								if(Float.parseFloat(colList[index].trim()) < Float.parseFloat(conditionColumnValue1.toString().trim()))
									colResult.add(line);
							}
							else if(lessThanEqTo1)
							{
								if(Float.parseFloat(colList[index].trim()) <= Float.parseFloat(conditionColumnValue1.toString().trim()))
									colResult.add(line);
							}
							else if(greaterThanEqTo1)
							{
								if(Float.parseFloat(colList[index].trim()) >= Float.parseFloat(conditionColumnValue1.toString().trim()))
									colResult.add(line);
							}
							else
							{
								errorRoutine(queryText);
								return;
							}
						}
					}
					catch (NumberFormatException e) 
					{
						e.printStackTrace();
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
					
					//Display the result
					
					//Print the column list
					for(int j = 0; j < columns.size(); j++)
						System.out.print(columns.get(j)+"\t");
					System.out.println();
					
					//for that may be I will get the display column list
					
					for(int k = 0; k < colResult.size(); k++)
					{
						String splitRow[] = colResult.get(k).split(",");
						
						for(int i = 0; i < miniSqlParser.tablesCreated.get(tableNames.get(0)).size(); i++)
						{
							for(int j = 0; j < columns.size(); j++)
							{
								if(getIndex(tableNames.get(0), miniSqlParser.tablesCreated.get(tableNames.get(0)).get(i))
										== getIndex(tableNames.get(0), columns.get(j)))
								{
									System.out.print(splitRow[getIndex(tableNames.get(0), columns.get(j))]+ "\t" );
									
								}		
							}
						}
						System.out.println();
					}
				}
			}
			else
			{
			
				/*
				 * Error handling
				 */
				
				// First check for the tables whether they are present or not			
				for(int i = 0; i < tableNames.size(); i++) 
				{
					if(!isTablePresent(tableNames.get(i)))
					{
						System.out.println("\"" +tableNames.get(i)+"\" Table does not exists.");
						return;
					}
				}
				
				//Then check for the column whether they are present in one of the table or not
				// again we can check for the ambigius column name in the table
				
				HashMap<String, Integer> isDone = new HashMap<String, Integer>();
				
				for(int i = 0; i < tableNames.size(); i++) 
				{
					for(int j = 0; j < columns.size(); j++) 
					{
						if(isColumnPresentInTable(tableNames.get(i), columns.get(j)))
						{
							if(!isDone.containsKey(columns.get(j)))
								isDone.put(columns.get(j), 1);
							else
							{
								System.out.println("One or more ambigiuous column in tables...");
								return;
							}
						}
					}	
				}
			
				// 	Now get the index of the columns from the tablecreated hashmap and loop over the tables i.e. csv file
				
				Vector<String> result = new Vector<String>();
				
				for(int i = 0; i < columns.size(); i++)
				{
					for(int j = 0; j < tableNames.size(); j++)
					{
						if(isColumnPresentInTable(tableNames.get(j), columns.get(i)))
						{
							int colIndex = getIndex(tableNames.get(j), columns.get(i));
							
							// Now read the file of the specific index after the tokenization
							
							String tableName = "src/" + tableNames.get(j).toString() + ".csv";
							File f = new File(tableName);
											
							if(f.exists())
							{
								// First get all the rows 
								BufferedReader tableFileReader = new BufferedReader(new FileReader(f));
								String rowInTable;
								int ctr = 0;
								try 
								{
									while((rowInTable = tableFileReader.readLine()) != null) 
									{
										String fields[] = rowInTable.split(",");
										if(ctr >= result.size() )
											result.add(ctr, fields[colIndex]);
										else
											result.set(ctr, result.get(ctr) + "," + fields[colIndex]);
										ctr++;
									}
								}
								catch (IOException e) 
								{				
									e.printStackTrace();
								}						
							}
						}				
					}
				}
				for (String string : result) 
				{
					String outputColumns[] = string.split(",");
					for(int i = 0; i < outputColumns.length; i++)
					{
						System.out.print(outputColumns[i]+"\t");
					}
					System.out.println();
				}
			}
		}
	}
	
	public static float getSumAggr(String colName, String tableName)
	{
		/*
		 * Error Handling
		 */ 
		
		//Check first if the table name is correct or not
		if(!isTablePresent(tableName))
		{
			System.out.println("\"" +tableName+"\" Table does not exists.");
			return -1;	
		}
		
		//Check if the column is the part of the table or not
		if(!isColumnPresentInTable(tableName, colName))
		{
			System.out.println("Column \""+colName +"\" is not part of the table \"" +tableName+"\"");
			return -1;	
		}
	
		// Get the row index in the table
		
		int rowIndex = getIndex(tableName, colName);
		float sum = 0;
		
		String tableNameFile = "src/" + tableName + ".csv";
		File f = new File(tableNameFile);
		
		if(f.exists())
		{
			// First get all the rows 
			BufferedReader tableFileReader = null;
			try 
			{
				tableFileReader = new BufferedReader(new FileReader(f));
			} 
			catch (FileNotFoundException e1) 
			{
			
				e1.printStackTrace();
			}
			String rowInTable;
			try 
			{
				while((rowInTable = tableFileReader.readLine()) != null) 
				{
					String colList[] = rowInTable.split(",");
					sum += Float.parseFloat(colList[rowIndex]);
				}
			}
			catch (IOException e) 
			{				
				e.printStackTrace();
			}
			
		}
		else
		{
			System.out.println("Table does not exists...");
			return -1;
		}
		return sum;
	}
	
	
	public static float getAvgAggr(String colName, String tableName)
	{
		/*
		 * Error Handling
		 */ 
		
		//Check first if the table name is correct or not
		if(!isTablePresent(tableName))
		{
			System.out.println("\"" +tableName+"\" Table does not exists.");
			return -1;	
		}
		
		//Check if the column is the part of the table or not
		if(!isColumnPresentInTable(tableName, colName))
		{
			System.out.println("Column \""+colName +"\" is not part of the table \"" +tableName+"\"");
			return -1;	
		}
	
		// Get the row index in the table
		
		int rowIndex = getIndex(tableName, colName);
		float sum = 0, count = 0;
		
		String tableNameFile = "src/" + tableName + ".csv";
		File f = new File(tableNameFile);
		
		if(f.exists())
		{
			// First get all the rows 
			BufferedReader tableFileReader = null;
			try 
			{
				tableFileReader = new BufferedReader(new FileReader(f));
			} 
			catch (FileNotFoundException e1) 
			{
				e1.printStackTrace();
			}
			String rowInTable;
			try 
			{
				while((rowInTable = tableFileReader.readLine()) != null) 
				{
					count++;
					String colList[] = rowInTable.split(",");
					sum += Float.parseFloat(colList[rowIndex]);
				}
			}
			catch (IOException e) 
			{				
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Table does not exists...");
			return -1;
		}
		return (float)sum/(float)count;
	}
	
	public static float getMaxAggr(String colName, String tableName)
	{
		/*
		 * Error Handling
		 */ 
		
		//Check first if the table name is correct or not
		if(!isTablePresent(tableName))
		{
			System.out.println("\"" +tableName+"\" Table does not exists.");
			return -1;	
		}
		
		//Check if the column is the part of the table or not
		if(!isColumnPresentInTable(tableName, colName))
		{
			System.out.println("Column \""+colName +"\" is not part of the table \"" +tableName+"\"");
			return -1;	
		}
	
		// Get the row index in the table
		
		int rowIndex = getIndex(tableName, colName);
		float max = Integer.MIN_VALUE;
		
		String tableNameFile = "src/" + tableName + ".csv";
		File f = new File(tableNameFile);
		
		if(f.exists())
		{
			// First get all the rows 
			BufferedReader tableFileReader = null;
			try 
			{
				tableFileReader = new BufferedReader(new FileReader(f));
			} 
			catch (FileNotFoundException e1) 
			{
				e1.printStackTrace();
			}
			String rowInTable;
			try 
			{
				while((rowInTable = tableFileReader.readLine()) != null) 
				{
					String colList[] = rowInTable.split(",");
					if(max < Float.parseFloat(colList[rowIndex]))
						max = Float.parseFloat(colList[rowIndex]);
				}
			}
			catch (IOException e) 
			{				
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Table does not exists...");
			return -1;
		}
		return max;
	}
	
	public static float getMinAggr(String colName, String tableName)
	{
		/*
		 * Error Handling
		 */ 
		
		//Check first if the table name is correct or not
		if(!isTablePresent(tableName))
		{
			System.out.println("\"" +tableName+"\" Table does not exists.");
			return -1;	
		}
		
		//Check if the column is the part of the table or not
		if(!isColumnPresentInTable(tableName, colName))
		{
			System.out.println("Column \""+colName +"\" is not part of the table \"" +tableName+"\"");
			return -1;	
		}
	
		// Get the row index in the table
		
		int rowIndex = getIndex(tableName, colName);
		float min = Integer.MAX_VALUE;
		
		String tableNameFile = "src/" + tableName + ".csv";
		File f = new File(tableNameFile);
		
		if(f.exists())
		{
			// First get all the rows 
			BufferedReader tableFileReader = null;
			try 
			{
				tableFileReader = new BufferedReader(new FileReader(f));
			}
			catch (FileNotFoundException e1) 
			{
				e1.printStackTrace();
			}
			String rowInTable;
			try 
			{
				while((rowInTable = tableFileReader.readLine()) != null) 
				{
					String colList[] = rowInTable.split(",");
					if(min > Float.parseFloat(colList[rowIndex]))
						min = Float.parseFloat(colList[rowIndex]);
				}
			}
			catch (IOException e) 
			{				
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Table does not exists...");
			return -1;
		}
		return min;
	}
	
	public static int getDistinctRows(String colName, String tableName)
	{
		/*
		 * Error Handling
		 */ 
		
		//Check first if the table name is correct or not
		if(!isTablePresent(tableName))
		{
			System.out.println("\"" +tableName+"\" Table does not exists.");
			return -1;	
		}
		
		//Check if the column is the part of the table or not
		if(!isColumnPresentInTable(tableName, colName))
		{
			System.out.println("Column \""+colName +"\" is not part of the table \"" +tableName+"\"");
			return -1;	
		}
	
		// Get the row index in the table		
		int rowIndex = getIndex(tableName, colName);
		
		Vector <String> rowValue = new Vector<String>();
		String tableNameFile = "src/" + tableName + ".csv";
		File f = new File(tableNameFile);
				
		if(f.exists())
		{
			// First get all the rows 
			BufferedReader tableFileReader = null;
			try 
			{
				tableFileReader = new BufferedReader(new FileReader(f));
			} 
			catch (FileNotFoundException e1) 
			{
				e1.printStackTrace();
			}
			String rowInTable;
			try 
			{
				while((rowInTable = tableFileReader.readLine()) != null) 
				{
					String colList[] = rowInTable.split(",");
					if(!rowValue.contains(colList[rowIndex]))
						rowValue.add(colList[rowIndex]);
				}
				
				//Print the distinct rows
				Iterator iterator = rowValue.iterator(); 
				
				while (iterator.hasNext())
				{
					System.out.println(iterator.next());
				}	
			}
			catch (IOException e) 
			{				
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Table does not exists...");
			return -1;
		}
		return 1;
	}	
		
	public static int getIndex(String tableName, String colName)
	{
		Vector<String> colList =  miniSqlParser.tablesCreated.get(tableName);
		int index = -1;
		for(int i = 0; i < colList.size(); i++)	
		{
			if(colList.get(i).toString().trim().equals(colName))
			{
				index = i;
				break;			
			}
		}
		return index;		
	}
	
	public static boolean isTablePresent(String tableName)
	{
		if(miniSqlParser.tablesCreated.containsKey(tableName)) 
			return true;
		else 
			return false;
	}
	
	public static boolean isColumnPresentInTable(String tableName, String colName)
	{
		if(miniSqlParser.tablesCreated.get(tableName).contains(colName)) 
			return true;
		else
			return false;
	}	
	
	public static void errorRoutine(String query)
	{
		System.out.println("Invalid Command...\n\""+query+"\"");
	}
	
	public static void parseTruncate(String queryText) throws FileNotFoundException
	{
		//Trim the trailing spaces if any
		//and also remove the "truncate" word from the query as we don't need it
		String query = queryText.substring("truncate".length(), queryText.length()).trim();
		String wordListInQuery[] = query.split(" ");
				
		//Check if the 
		if(wordListInQuery.length != 2 || !wordListInQuery[0].equalsIgnoreCase("table"))
		{
			errorRoutine(queryText.trim());
			return;
		}
		
		String tableName = wordListInQuery[1].toString(); 
		if(!isTablePresent(tableName))
		{
			System.out.println("\"" +tableName+"\" Table does not exists.");
			return;
		}
		
		//Delete all the record from the file 
		try 
		{
			FileOutputStream output = new FileOutputStream("src/" + tableName + ".csv", false);
			output.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		System.out.println("\""+tableName+"\" truncated successfully...");
	}
	
	public static void parseDrop(String queryText) throws FileNotFoundException, IOException
	{
		//Trim the trailing spaces if any
		//and also remove the "truncate" word from the query as we don't need it
		String query = queryText.substring("drop".length(), queryText.length()).trim();
		String wordListInQuery[] = query.split(" ");
				
		//Check if the 
		if(wordListInQuery.length != 2 || !wordListInQuery[0].equalsIgnoreCase("table"))
		{
			errorRoutine(queryText.trim());
			return;
		}
		
		String tableName = wordListInQuery[1].toString(); 
		if(!isTablePresent(tableName))
		{
			System.out.println("\"" +tableName+"\" Table does not exists.");
			return;
		}
		
		String fileName = "src/" + tableName + ".csv";
		File f = new File(fileName);
		
		boolean deleteStatus = f.delete();
		if(!deleteStatus)
		{
			System.out.println("Unable to remove file (System error)...");
			return;
		}
		miniSqlParser.tablesCreated.remove(tableName);
		miniSqlParser.writeToMetaFile();
		System.out.println("\""+tableName+"\" deleted successfully...");
	}
	
	public static void parseInsert(String query)
	{
		String queryOrg = query;
		String queryToken[] = query.split(" ");
		
		//Check for the correct syntax of "into" word
		if(!queryToken[1].toLowerCase().equals("into"))
		{
			errorRoutine(queryOrg);
			return;
		}
		
		//Get the tablename
		String tableName;
		if(queryToken[2].contains("("))
			tableName = queryToken[2].substring(0, queryToken[2].indexOf('('));
		else
			tableName = queryToken[2].trim();
		
		//Check if the table is present or not
		if(!isTablePresent(tableName))
		{
			System.out.println("\"" + tableName + "\" Table does not exists.");
			return;
		}		
		
		//Change multiple spaces to the single spaces
		query = query.replaceAll("( )+", " ");
		
		final Pattern pattern = Pattern.compile("\\((.+?)\\)");
		final Matcher matcher = pattern.matcher(query);
		if(!matcher.find())
		{
			errorRoutine(query);
			return;
		}
		String queryValueText = matcher.group(1);
		int startPosOfPar = matcher.start();
		
		//Check for the values syntax i.e the text between tableName end and ( start
		if(!query.substring(query.indexOf(tableName) + tableName.length(), startPosOfPar).toLowerCase().trim().equals("values"))
		{
			errorRoutine(queryOrg);
			return;
		}
		
		// Final ) is concatinated to the final columns
		
		String columnValue = query.substring(query.indexOf("(") + 1, query.indexOf(")")).trim();
		
		String Values[] = columnValue.split(",");
		
		//Check if number of comma's + 1 is more than the column count, if yes then error
		int commaCount = columnValue.length() - columnValue.replaceAll(",", "").length();
		if((commaCount + 1) != Values.length)
		{
			errorRoutine(queryOrg);
			return ;
		}
		
		//Check if the query  contains more number of columns
		if(commaCount + 1 != miniSqlParser.tablesCreated.get(tableName).size())
		{
			System.out.println("More/Less number of columns in the query...");
			return;
		}
				
		//Check whether the values are of float type or not
		for(int i = 0; i < Values.length; i++)
		{
			try
			{
				Float.parseFloat(Values[i]);
			}
			catch(Exception e)
			{
				System.out.println("Data type mismatch...");
				return;
			}
		}		
		
		FileWriter f = null;
		try 
		{
			f = new FileWriter("src/" + tableName + ".csv", true);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		try 
		{
			BufferedReader fr = new BufferedReader(new FileReader("src/" + tableName + ".csv"));
			if(fr.readLine() !=  null)
				f.write("\n" + columnValue);
			else
				f.write(columnValue);
			f.close();
			fr.close();
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
		System.out.println("Record inserted successfully...");
	}
	
	public static void parseCreate(String queryText)
	{
		//Check if the parenthesis is there or not 
		if(queryText.indexOf('(') == -1)
		{
			errorRoutine(queryText);
			return;
		}
		
		String subPart[] = queryText.substring("create".length(), queryText.indexOf('(')).trim().split(" ");
		
		//Check for the corner condition
		if(subPart.length != 2 || !subPart[0].toLowerCase().equals("table"))
		{
			errorRoutine(queryText);
			return;
		}
		
		String tableName = subPart[1];
		
		//Check if the table is already present or not if present throw error
		if(isTablePresent(tableName))
		{
			System.out.println("\""+tableName+"\" already present...");
			return;
		}
		
		Vector<String> colNameList = new Vector<String>();
		Vector<String> colNameDataTypeList = new Vector<String>();
		
		//create table emp(emp_id INT  ,  emp_age  INT  );  ->> Error
		
		String orgQuery = queryText;
		queryText =  queryText.replaceAll("( )+", " ");
		
		String fullColList[] = queryText.substring(queryText.indexOf('(')+1, queryText.indexOf(')') + 1).trim().split(",");
				
		for(int i = 0; i < fullColList.length; i++)
		{
			String attrList[] = fullColList[i].trim().split(" ");
			if(attrList.length != 2)
			{
				errorRoutine(orgQuery);
				return;
			}
			colNameList.add(attrList[0]);
			colNameDataTypeList.add(attrList[1]);
		}
		
		miniSqlParser.tablesCreated.put(tableName, colNameList);
		try 
		{
			miniSqlParser.writeToMetaFile();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		FileWriter f = null;
		try 
		{
			f = new FileWriter("src/" + tableName+".csv");
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		try 
		{
			f.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		System.out.println("Created \""+tableName+"\" table successfully...");
	}
	
	public static void parseDelete(String query) throws FileNotFoundException
	{
		String queryOrg = query;
		String tokens[] = query.split(" ");
		String tableName ;
		
		try
		{
			//Check for the "from"
			if(!tokens[1].toLowerCase().trim().equals("from"))
			{
				errorRoutine(queryOrg);
				return;
			}
		}
		catch(Exception e)
		{
			errorRoutine(queryOrg);
			return;
		}
		try
		{
		//Check for the correct "tablename"
		tableName = tokens[2].trim();
		if(!isTablePresent(tokens[2].trim()))
		{
			System.out.println("\""+tokens[2].trim()+"\" table is not present...");
			return;
		}
		}
		catch(Exception e)
		{
			errorRoutine(queryOrg);
			return;
		}
		
		try
		{
			//Check for the correct "where"
			if(!tokens[3].toLowerCase().trim().equals("where"))
			{
				errorRoutine(queryOrg);
				return;
			}
		}
		catch(Exception e)
		{
			errorRoutine(queryOrg);
			return;
		}
		//"Delete" "from" "<table­name>" "where" <attribute> = <some­value>
		
		if(query.indexOf('=') != -1)
		{
			if(query.indexOf('<') != -1)
				query = query.replaceAll("<="," <= ");				
			else if(query.indexOf('>') != -1)
				query = query.replaceAll(">="," >= ");
			else
				query = query.replaceAll("="," = ");
		}
		else if(query.indexOf('<') != -1)
		{
			query = query.replaceAll("<"," < ");
		}
		else if(query.indexOf('>') != -1)
		{
			query = query.replaceAll(">"," > ");
		}
		
		query = query.replaceAll("( )+"," ");
		
		query = query.substring(query.indexOf("where")+"where".length(), query.length()).trim();
		
		String values[] = query.split(" ");
		if(values.length != 3)
		{
			errorRoutine(queryOrg);
			return;
		}
			
		String colName = values[0];
		String condition = values[1].trim();
		String value = values[2];
		String line;
		Vector <String> colResult = new Vector<String>();
		int index = getIndex(tableName, colName);
		
		try 
		{
			BufferedReader f = new BufferedReader(new FileReader("src/" + tableName + ".csv"));
			while((line = f.readLine()) != null)
			{
				String colList[] = line.split(",");
				switch(condition)
				{
					case "=":
						if(!colList[index].trim().equals(value.trim()))
							colResult.add(line);
						break;
					case ">":
						if(Float.parseFloat(colList[index].trim()) <= Float.parseFloat(value.trim()))
							colResult.add(line);
						break;
					case "<":
						if(Float.parseFloat(colList[index].trim()) >= Float.parseFloat(value.trim()))
							colResult.add(line);
						break;
					case "<=":
						if(Float.parseFloat(colList[index].trim()) > Float.parseFloat(value.trim()))
							colResult.add(line);
						break;
					case ">=":
						if(Float.parseFloat(colList[index].trim()) < Float.parseFloat(value.trim()))
							colResult.add(line);
						break;
					default:
						errorRoutine(queryOrg);
						return;
				}
			}
			f.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		try
		{
			FileWriter f = new FileWriter("src/" + tableName + ".csv");
			for(int i = 0; i < colResult.size(); i++)
			{
				f.write(colResult.get(i));
				if(i + 1 != colResult.size())
					f.write("\n");
			}
			f.close();
		}
		catch (Exception e)
		{
			System.out.println("Error in updation...");
			return;
		}
		System.out.println("Record deleted successfully...");
	}
}

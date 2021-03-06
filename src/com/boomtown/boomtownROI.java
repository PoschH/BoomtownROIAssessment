//Assessment completed by Hannah Carl

package com.boomtown;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class boomtownROI {
	
	//Method to get the API Information from URL provided
	public static String[] getAPIInformation(String urlString) throws IOException {
		String lineFromAPI = "";
		String [] apiInfo;
		
		//Set url and read lines from API
		URL url = new URL (urlString);
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		lineFromAPI = reader.readLine();
		
		//Clean up api lines
		apiInfo = lineFromAPI.split(",|\\{|\\}");
		
		reader.close();
		
		return apiInfo;
		
		
	}
	//Method to get the HTTP status code 
	public static int getHTTPCode(String urlString) throws IOException {
		int httpCode = 0;
		
		//Set url and make connection
		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		
		//Get response code
		httpCode = connection.getResponseCode();
		
		connection.disconnect();
		
		return httpCode;
	}
	//Method to parse the IDs from the API information
	public static ArrayList<String> parseApiId (String[] apiURLInfoList) {
		String[] tempList = null;
		ArrayList<String> idValuesParsed = new ArrayList<String>();
		String cleanIdString = "";	//Clean ID String
		String cleanName = "";		//Clean Object Name for ID
		
		//Loop to find IDs and remove excess characters
		for(int i =0; i < apiURLInfoList.length; i++) {
			if(apiURLInfoList[i].indexOf("\"id\":") != -1){
				tempList = apiURLInfoList[i].split("\"id\"|\\:|\\,");
				cleanIdString = tempList[2].replaceAll("\"|\\s", "");
				
				//Find name of object for id
				//Will exclude first line of api to prevent exception
				if(i != 0 ) {
					cleanName = apiURLInfoList[i-1].replaceAll("[\\{ \":,\\[]", "");
				}
				//Add clean name and clean id string to list
				if(!cleanName.equals("")) {
					idValuesParsed.add(cleanName);
				}
				idValuesParsed.add(cleanIdString);	
			}
		}
		return idValuesParsed;
	}
	//Method to output the api information found
	public static void outputAPIInformation(String[] apiInfoList) throws IOException {
		ArrayList<String> boomtownURLList = new ArrayList<String>();	
		ArrayList<String> idValuesFromURL = new ArrayList<String>();
		int httpCode;
		String[] apiInfoFromURL;
		
		//Loop to find all urls with api.github.com/orgs/BoomTownROI
		for(int i = 0; i < apiInfoList.length; i++) {
			if(apiInfoList[i].indexOf("api.github.com/orgs/BoomTownROI") != -1) {
				String[] urlLine = apiInfoList[i].split("\"");
				boomtownURLList.add(urlLine[3]);
			}
		}
		//Loop to check http code and output correct ids
		for(int i = 0; i < boomtownURLList.size(); i++) {
			httpCode = getHTTPCode(boomtownURLList.get(i));
			
			//If http code is 200
			if(httpCode ==200) {
				//Get api info and parse id values out
				apiInfoFromURL = getAPIInformation(boomtownURLList.get(i));
				idValuesFromURL = parseApiId(apiInfoFromURL);
				
				//Output Title
				System.out.println("\nStatus Code: " + httpCode + " URL: " + boomtownURLList.get(i) );
				
				//Loop to print IDs and Objects associated
				for(int j = 0; j < idValuesFromURL.size(); j++) {
					//Check if all digits
					if(idValuesFromURL.get(j).matches("[0-9]+")) {
						System.out.println(" ID: " + idValuesFromURL.get(j));
					}
					else {
						System.out.print("	Object: " + idValuesFromURL.get(j) + "; ");
					}
				}
			}
			//If http code anything else
			else {
				System.out.println("\nFailed Request" + " Status Code: " + httpCode + " URL: "+ boomtownURLList.get(i) );
			}
			
		}	
		
	}
	//Method to verify date information
	public static void verifyDateInformation(String[] apiInformationList) {
		Boolean datesCorrect = false;
		String createdTime = "";
		String updatedTime = "";
		
		//Loop to find created, updated times and clean up string
		for(int i = 0; i < apiInformationList.length; i++) {
			if(apiInformationList[i].indexOf("\"created_at\":") != -1) {
				createdTime = apiInformationList[i].replaceAll("\\s|created_at|\":|\\,|\"", "");
			}
			if(apiInformationList[i].indexOf("\"updated_at\":") != -1) {
				updatedTime = apiInformationList[i].replaceAll("\\s|updated_at|\":|\\,|\"", "");
			}
		}
		//Compare the timestamps
		if(createdTime.compareTo(updatedTime) < 0) {
			datesCorrect = true;
		}
		
		//Output
		System.out.println("\nDate Verified: " +datesCorrect + "\nCreated Time: " + createdTime + " Updated Time: " + updatedTime);

	}
	
	//Method to verify repository count
	public static void compareReposCount(String[] apiInformationList) throws IOException {
		String repositoryURL = "";
		String repositoryURLWithPageNumber = "";
		String[] reposApi;
		int publicRepos = 0;
		int reposCounter = 0;
		int pageNumber = 1;
		
		//Loop to find public_repos, repos_url and clean up string
		for(int i = 0; i < apiInformationList.length; i++) {
			if(apiInformationList[i].indexOf("\"public_repos\":") != -1) {
				publicRepos = Integer.parseInt(apiInformationList[i].replaceAll("\\s|public_repos|:|\\,|\"", ""));
			}
			if(apiInformationList[i].indexOf("\"repos_url\":") != -1) {
				repositoryURL = apiInformationList[i].replaceAll("\\s|repos_url|\":|\\,|\"", "");
			}
		}
		
		//Loop to count repositories from repos page
		while (reposCounter < publicRepos) {
			//Add page number to url
			repositoryURLWithPageNumber = repositoryURL + "?page=" + pageNumber;
			
			//Get api information from repository api
			reposApi = getAPIInformation(repositoryURLWithPageNumber);
		
			//Count how many repositories are listed
			for(int i =0; i < reposApi.length; i++) {
				if(reposApi[i].indexOf("\"full_name\":") != -1) {
					reposCounter++;
				}
			}
			//Increment page number
			pageNumber++;
		}
		
		//Output
		if(reposCounter == publicRepos) {
			System.out.println("\nRepository Counter: Verified");
			System.out.println("Repository Count: "+ reposCounter);
		}
		else {
			System.out.println("\nRespository Counter: Not Verified " + reposCounter);
		}

	}

	//Main method
	public static void main(String[] args) throws IOException {
		
		String url = "https://api.github.com/orgs/boomtownroi";
		
		String [] apiInformation = getAPIInformation(url);
		outputAPIInformation(apiInformation);
		verifyDateInformation(apiInformation);
		compareReposCount(apiInformation);
			
	}

}

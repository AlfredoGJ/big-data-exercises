
package nearsoft.academy.bigdata.recommendation;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.common.*;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender; 
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;






public class MovieRecommender {

    private FastByIDMap<PreferenceArray> usersPreferences; 
    private HashMap<String,Long> userIDs;
    private HashMap<String,Long> productIDs;
    private long userCount;
    private long productCount;
    private long reviewCount;
    private GenericDataModel model;
    private GenericUserBasedRecommender recommender;


    public MovieRecommender(String filePath) {

        productIDs = new HashMap<String,Long>();
        userIDs = new HashMap<String,Long>();
        usersPreferences = new FastByIDMap<PreferenceArray>();
        userCount = 0;
        productCount =0;
        reviewCount= 0;
        


        System.out.println("Hello Java Again :(\n");
        getPreference(filePath);
        buildRecomender();
        // printPreferences();

    }


    GenericPreference getPreference(String filePath) {
        String productID = "";
        String userID = "";
        Float score = 0.0f;

        BufferedReader reader;


        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line = reader.readLine();
            while (line != null) {
            

                if(line.startsWith("product/productId:") )
                    productID = line.split(" ")[1];
                if(line.startsWith("review/userId:") )
                    userID = line.split(" ")[1];
                if(line.startsWith("review/score:") ){
                   
                    score = Float.parseFloat( line.split(" ")[1]); 
                    addPreference(userID, productID,score);
                    
                    
                }
                line = reader.readLine();
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    void addPreference( String userID,String productID, float score)
    {
        reviewCount++;
        System.out.println("Reviews Processed: "+ reviewCount);
        if(!userIDs.containsKey(userID) )
            userIDs.put(userID,++userCount);
        
       
        if(!productIDs.containsKey(productID) )
            productIDs.put(productID,++productCount);

        Long longUserID = userIDs.get(userID);
        Long longProductID = productIDs.get(productID);
    
        GenericUserPreferenceArray preferencesArray = (GenericUserPreferenceArray)usersPreferences.get(longUserID);
        ArrayList preferencesList = new ArrayList();

        // Checking if there was previous preferences for this user
        if(preferencesArray != null)
        {
            // Move the preferences from the array to a list
            for(Integer i =0; i< preferencesArray.length();i ++ )
                preferencesList.add(preferencesArray.get(i));
        }

        // Add the new preference to the list
        preferencesList.add(new GenericPreference(longUserID, longProductID, score));

        // Get preferences back to the array inside the FastByIDMap
        usersPreferences.put(longUserID, new GenericUserPreferenceArray(preferencesList));

    }

    void printPreferences()
    {
        for (int i = 1; i <= usersPreferences.size();i++) 
        {
            GenericUserPreferenceArray preferences= (GenericUserPreferenceArray)usersPreferences.get(i);
            System.out.println("UserID: " + i + "\n" );

            for(int p =0;p < preferences.length();p ++)
            {
                System.out.println("\tUser: " + preferences.get(p).getUserID() + " Product: " + preferences.get(p).getItemID() + " Score: " + preferences.get(p).getValue()  );

            }
        }
    }


    void buildRecomender()
    {   
        model= new GenericDataModel(usersPreferences);
        try{
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);    
        }catch(Exception e)
        {

        }
           

    }

    public int getTotalReviews() {

        return (int)reviewCount;
    }

    public int getTotalProducts() {
        // return 253059;
        return model.getNumItems();
    }

    public int getTotalUsers() {
        // return 889176;
        return model.getNumUsers();
    }

    List<String> getRecommendationsForUser(String user) {
        
        List<String> result = new ArrayList<String>();
        System.out.println("user: "+ userIDs);
        long longUserID = userIDs.get(user);
        System.out.println("user: " + userIDs);

        try{

            List<RecommendedItem>recomendations =  recommender .recommend(longUserID,10);
            for(RecommendedItem item: recomendations )
            {
                result.add((String)productIDs.keySet().toArray()[(int)item.getItemID()]);
            }
        }
        catch(Exception e)
        {

        }
        
        //  result.add("B0002O7Y8U");
        //  result.add("B00004CQTF");
        //  result.add("B000063W82");
        return result;
    }



}

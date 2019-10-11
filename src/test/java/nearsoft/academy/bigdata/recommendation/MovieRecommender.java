
package nearsoft.academy.bigdata.recommendation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.*;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

public class MovieRecommender {

    private FastByIDMap<PreferenceArray> usersPreferences;
    private HashMap<String, Long> userIDs;
    private BiMap<String, Long> productIDsBi;

    private long userCount;
    private long productCount;
    private long reviewCount;
    private GenericDataModel model;
    private GenericUserBasedRecommender recommender;

    public MovieRecommender(String filePath) throws TasteException {

        productIDsBi = HashBiMap.create();
        userIDs = new HashMap<String, Long>();
        usersPreferences = new FastByIDMap<PreferenceArray>();
        userCount = 0;
        productCount = 0;
        reviewCount = 0;

        getPreference(filePath);
        buildRecomender();

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

                if (line.startsWith("product/productId:"))
                    productID = line.split(" ")[1];
                if (line.startsWith("review/userId:"))
                    userID = line.split(" ")[1];
                if (line.startsWith("review/score:")) {

                    score = Float.parseFloat(line.split(" ")[1]);
                    addPreference(userID, productID, score);

                }
                line = reader.readLine();
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    void addPreference(String userID, String productID, float score) {
        
        reviewCount++;
        if (!userIDs.containsKey(userID))
            userIDs.put(userID, ++userCount);

        if (!productIDsBi.containsKey(productID))
            productIDsBi.put(productID, ++productCount);

        Long longUserID = userIDs.get(userID);
        Long longProductID = productIDsBi.get(productID);


        addPreferenceToUser((GenericUserPreferenceArray) usersPreferences.get(longUserID),new GenericPreference(longUserID, longProductID, score));

        // GenericUserPreferenceArray preferencesArray = (GenericUserPreferenceArray) usersPreferences.get(longUserID);
        // ArrayList preferencesList = new ArrayList();

        // if (preferencesArray != null) {
        //     for (Integer i = 0; i < preferencesArray.length(); i++)
        //         preferencesList.add(preferencesArray.get(i));
        // }

        // preferencesList.add(new GenericPreference(longUserID, longProductID, score));

        // usersPreferences.put(longUserID, new GenericUserPreferenceArray(preferencesList));

    }
    void addPreferenceToUser(GenericUserPreferenceArray existentPreferences, GenericPreference newPreference){

        ArrayList preferencesList = new ArrayList();

        if (existentPreferences != null) {
            for (Integer i = 0; i < existentPreferences.length(); i++)
                preferencesList.add(existentPreferences.get(i));
        }

        preferencesList.add(newPreference);

        usersPreferences.put(newPreference.getUserID(), new GenericUserPreferenceArray(preferencesList));

    }


    void buildRecomender() throws TasteException {
        model = new GenericDataModel(usersPreferences);
        
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
    }
    
    public int getTotalReviews() {

        return (int) reviewCount;
    }

    public int getTotalProducts() {
        return model.getNumItems();
    }

    public int getTotalUsers() {
        return model.getNumUsers();
    }

    List<String> getRecommendationsForUser(String user) throws TasteException {
        
        List<String> result = new ArrayList<String>();
        long longUserID = userIDs.get(user);
            List<RecommendedItem>recomendations =  recommender .recommend(longUserID,3);

            for(RecommendedItem item: recomendations )
            {
                result.add((String)productIDsBi.inverse().get(item.getItemID()));
            }
        return result;
    }
}

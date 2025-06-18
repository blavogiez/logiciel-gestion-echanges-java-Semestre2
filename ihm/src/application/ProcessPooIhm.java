package application;

import java.util.ArrayList;
import java.util.List;

public class ProcessPooIhm {

    // public static CountryVisit retriveCountryVisitOfSimpleString(String cvString, List<CountryVisit> cvList){
    //     int index = 0;
    //     while(index < cvList.size() && !cvList.get(index).toSimpleString().equals(cvString)){
    //         index ++;
    //     }
    //     return cvList.get(index);
    // }

    public static List<String> exchListToStrings(List<Exchange> exList){
        List<String> exString = new ArrayList<String>();

        for(Exchange ex : exList){
            exString.add(ex.toGraphicalString());
        }

        return exString;
    }
}

package com.sword.gsa.spis.scs.utils.string;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**  how to split a string into fixed length rows, without “breaking” the words. */
public class TextSplitter {

    public static List<String> splitString(String msg, int lineSize) {
        List<String> res = new ArrayList<String>();

        Pattern p = Pattern.compile("\\b.{1," + (lineSize-1) + "}\\b\\W?");
        Matcher m = p.matcher(msg);

        while(m.find()) {
           // System.out.println(m.group().trim());   // Debug
            res.add(m.group());
        }
        return res;
    }

   /* public static void main(String[] args) {

        splitString("In this case a special character appears in the last position of the row length!",80);
        System.out.println("----");
        splitString("In this case a special character appears as first position of the brand new line!",80);



In this case a special character appears in the last position of the row length!
----
In this case a special character appears as first position of the brand new
line!

    }  */

}
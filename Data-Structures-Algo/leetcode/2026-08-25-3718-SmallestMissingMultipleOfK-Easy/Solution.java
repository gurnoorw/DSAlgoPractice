/*
https://leetcode.com/problems/shortest-and-lexicographically-smallest-beautiful-string/
 */
class Solution {
    public String shortestBeautifulSubstring(String s, int k) {
        int n = s.length();
        String smallest = s;

        int ones = 0;
        int left = 0;
        int total = 0;
        for(int right = 0; right < n; right++){
            if(s.charAt(right) == '1'){
                ones++;
                total++;
            }
            while(ones > k && left <= right){
                if(s.charAt(left) == '1'){
                    ones--;
                }
                left++;
            }
            while(left <= right && s.charAt(left) == '0' ){
                left++;
            }
            if(ones == k &&
                    (s.substring(left , right + 1).length() < smallest.length()
                            || ( s.substring(left , right + 1).length() == smallest.length()
                            && s.substring(left , right + 1).compareTo(smallest) < 0))){
                smallest = s.substring(left , right + 1);
            }
        }

        if(k > total){
            return "";
        }
        return smallest;
    }
}
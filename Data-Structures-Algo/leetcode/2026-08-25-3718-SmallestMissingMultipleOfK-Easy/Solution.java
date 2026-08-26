/* https://leetcode.com/problems/count-subarrays-with-majority-element-ii/
* O(n) solution
* */
class Solution {

    public int missingMultiple(int[] nums, int k) {
        Set<Integer> seen = new HashSet<>();
        for (int num : nums) {
            seen.add(num);
        }
        int ans = k;
        while (seen.contains(ans)) {
            ans += k;
        }
        return ans;
    }
}
/*
O(nLogN) solution
----

class Solution {

    public int missingMultiple(int[] nums, int k) {
        Set<Integer> set = new TreeSet<>();
        for(int num : nums){
            if(num % k == 0){
                set.add(num / k);
            }
        }

        int lastFound = 0;
        for(int key : set){
            if(key - lastFound > 1){
                return (lastFound + 1 ) * k;
            }
            else{
                lastFound = key;
            }
        }
        return (lastFound + 1 ) * k;
    }
}


 */
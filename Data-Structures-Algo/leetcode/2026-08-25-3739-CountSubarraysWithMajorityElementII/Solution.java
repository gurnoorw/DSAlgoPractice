/*

https://leetcode.com/problems/count-subarrays-with-majority-element-ii/
*/
class Solution {
    public long countMajoritySubarrays(int[] nums, int target) {
        int n = nums.length;

        int offset = n + 1;
        int maxVal = 2 * n + 2;

        FenwickTree bit = new FenwickTree(maxVal);

        long validSubarrays = 0;
        int currentSum = 0;

        // Base case: prefix sum before indexing begins is 0
        bit.update(0 + offset, 1);

        for (int num : nums) {
            currentSum += (num == target ? 1 : -1);

            int shiftedSum = currentSum + offset;

            // Count how many previous prefix sums are strictly smaller than shiftedSum
            validSubarrays += bit.query(shiftedSum - 1);

            // Add the current prefix sum into the BIT for future right endpoints
            bit.update(shiftedSum, 1);
        }

        return validSubarrays;

    }
}

class FenwickTree{
    int size;
    int[] tree;

    FenwickTree(int size){
        this.size = size;
        this.tree = new int[size + 1];
    }

    public void update(int i, int val){
        while(i <= size){
            tree[i] += val;
            i += i & (-i);
        }
    }

    public int query(int i){
        int sum = 0;
        while(i > 0){
            sum += tree[i];
            i -= i & (-i);
        }
        return sum;
    }
}
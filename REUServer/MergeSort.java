/*
 * 	Code Taken from: 
 * 	http://www.java2novice.com/java-sorting-algorithms/merge-sort/
 * 	Now that I learned how to compare Paillier Encrypted Values:
 * 	Following this guide, I can run the C++ in Java...
 * 	https://www.ibm.com/developerworks/java/tutorials/j-jni/j-jni.html
 */

public class MergeSort
{
	private int [][] array;
	private int [][] tempMergArr;
	private int length;
	final static int PAIRS = 10;
	
	public static void main(String args[])
	{
		int [][] inputArr =
			{
				{45,1},
				{23,2},
				{11,3},
				{89,4},
				{77,5},
				{98,6},
				{4,7},
				{28,8},
				{65,9},
				{43,10}
			};
		
		MergeSort sort = new MergeSort();
		sort.sort(inputArr);
		
		for(int i=0;i<PAIRS;i++)
		{
		    System.out.print(inputArr[i][0] + " " + inputArr[i][1]);
		    System.out.println(" ");
	    }		
	}
	
	public MergeSort ()
	{
	}
	
	public void sort(int [][] inputArr)
	{
		this.array = inputArr;
		this.length = PAIRS;
		this.tempMergArr = new int[PAIRS][2];
		doMergeSort(0, length - 1);
	}

	private void doMergeSort(int lowerIndex, int higherIndex)
	{
		if (lowerIndex < higherIndex)
		{
			int middle = lowerIndex + (higherIndex - lowerIndex) / 2;
			// Below step sorts the left side of the array
			doMergeSort(lowerIndex, middle);
			// Below step sorts the right side of the array
			doMergeSort(middle + 1, higherIndex);
			// Now merge both sides
			mergeParts(lowerIndex, middle, higherIndex);
		}
	}

	private void mergeParts(int lowerIndex, int middle, int higherIndex)
	{
		for (int i = lowerIndex; i <= higherIndex; i++)
		{
			tempMergArr[i] = array[i];
		}
		int i = lowerIndex;
		int j = middle + 1;
		int k = lowerIndex;
		while (i <= middle && j <= higherIndex)
		{
			//Implement the DGK, enc (x) <= enc(y)
			//But for now, just do simple C++ code to test the wrapper...
			if (tempMergArr[i][0] <= tempMergArr[j][0])
			{
				array[k] = tempMergArr[i];
				i++;
			} 
			else
			{
				array[k] = tempMergArr[j];
				j++;
			}
			k++;
		}
		while (i <= middle)
		{
			array[k] = tempMergArr[i];
			k++;
			i++;
		}
	}
}
package it.unibz.inf.data_pumper.utils;

import java.util.List;

public class UtilsMath {

    public static long GCD(long a, long b) {
	if (b==0) return a;
	return GCD(b,a%b);
    }

    public static long lcm(List<Number> input){
	long result = input.get(0).longValue();
	for(int i = 1; i < input.size(); i++) result = lcm(result, input.get(i).longValue());
	return result;
    }

    public static long lcm(long[] input)
    {
	long result = input[0];
	for(int i = 1; i < input.length; i++) result = lcm(result, input[i]);
	return result;
    }

    private static long lcm(long a, long b)
    {
	return a * (b / GCD(a, b));
    }
}

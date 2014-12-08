package gla.sowf.services;
public class DistanceCalculator {
	public double calculateDistance(int xA, int yA,int xB, int yB)
	{
		int a=(xA - xB) * (xA - xB);
		int b=(yA - yB) * (yA - yB);
		return Math.sqrt(a+b);
	}
}

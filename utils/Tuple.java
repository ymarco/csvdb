package utils;

public class Tuple<F, S> { //<First,Second> 
	public final F f;
	public final S s;
	
	public Tuple(F f, S s) { 
		this.f = f; 
		this.s = s; 
	} 
}
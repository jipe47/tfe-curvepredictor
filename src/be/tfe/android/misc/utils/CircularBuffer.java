package be.tfe.android.misc.utils;

import java.util.Deque;
import java.util.LinkedList;


public class CircularBuffer<O> {
	
	private Deque<O> deque;
	
	public CircularBuffer()
	{
		this.deque = new LinkedList<O>();
	}
	
	public void add(O object)
	{
		deque.addLast(object);
	}
	
	public O top()
	{
		return deque.getFirst();
	}
	
	public O removeTop()
	{
		return deque.removeFirst();
	}
	
	public O next()
	{
		deque.addLast(deque.removeFirst());
		return top();
	}
	
	public int size()
	{
		return deque.size();
	}

}

/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.util;

import java.util.Stack;

import leola.vm.types.LeoArray;
import leola.vm.types.LeoDouble;
import leola.vm.types.LeoInteger;
import leola.vm.types.LeoLong;
import leola.vm.types.LeoMap;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;

/**
 * A simple shim to work with java to convert the {@link LeoObject}.
 * 
 * Useful for serializing config files
 * 
 * @author Tony
 *
 */
public class Shim {

	private Stack<LeoObject> stack = new Stack<LeoObject>();
	
	
	public Shim map() {		
		stack.add(new LeoMap());
		return this;
	}
	
	public Shim array() {
		stack.add(new LeoArray());
		return this;
	}
	
	public Shim put(String v, int n) {
		stack.peek().setObject(LeoString.valueOf(v), LeoInteger.valueOf(n));
		return this;
	}
	
	public Shim put(String v, long n) {
		stack.peek().setObject(LeoString.valueOf(v), new LeoLong(n));
		return this;
	}
	
	public Shim put(String v, double n) {
		stack.peek().setObject(LeoString.valueOf(v), LeoDouble.valueOf(n));
		return this;
	}
	
	public Shim put(LeoObject k, LeoObject v) {
		stack.peek().setObject(k, v);
		return this;
	}
	
	public Shim put(String k, Shim v) {
		LeoObject val = v.pop();
		stack.peek().setObject(LeoString.valueOf(k), val);
		return this;
	}
	
	public Shim add(LeoObject v) {
		stack.peek().$add(v);
		return this;
	}
	
	public Shim add(String v) {
		return add(LeoString.valueOf(v));
	}
	
	public Shim add(int v) {
		return add(LeoInteger.valueOf(v));
	}
	public Shim add(long v) {
		return add(new LeoLong(v));
	}
	public Shim add(double v) {
		return add(LeoDouble.valueOf(v));
	}
	public Shim add(Shim v) {
		return add(v.pop());
	}
	
	public LeoObject pop() {
		return stack.pop();
	}
	
	public static void main(String [] args) throws Exception {
		Shim s = new Shim();
		s.map()
			.put("cell", s.map().put("width", 256)
							    .put("height", 256))
		    .put("map", s.array()
		    				.add(s.array().add(1).add(1).add(1).add(1).add(1).add(1))
		    				.add(s.array().add(1).add(0).add(0).add(0).add(0).add(1))
		    				.add(s.array().add(1).add(0).add(0).add(0).add(0).add(1))
		    				.add(s.array().add(1).add(0).add(0).add(0).add(0).add(1))
		    				.add(s.array().add(1).add(0).add(0).add(0).add(0).add(1))
		    				.add(s.array().add(1).add(1).add(1).add(1).add(1).add(1)));

		LeoObject m = s.pop();
		System.out.println(m);
	}
}

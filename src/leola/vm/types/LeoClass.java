/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Method;

import leola.vm.ClassDefinition;
import leola.vm.Leola;
import leola.vm.asm.Bytecode;
import leola.vm.asm.Scope;
import leola.vm.util.ClassUtil;

/**
 * Represents an instance of a Class
 * 
 * @author Tony
 *
 */
public class LeoClass extends LeoScopedObject {

	/**
	 * Metaclass is used for reflection
	 * 
	 * @author Tony
	 *
	 */
	public static class Metaclass {
		private LeoClass clss;
		
		/**
		 * @param clss
		 */
		public Metaclass(LeoClass clss) {
			this.clss = clss;
		}
		
		public LeoString className() {
			return clss.className;
		}
		
		/**
		 * @see LeoScopedObject#getProperties()
		 * @return
		 */
		public LeoArray members() {
			return clss.getProperties();
		}
		
		/**
		 * @see LeoScopedObject#getPropertyNames()
		 * @return
		 */
		public LeoArray memberNames() {
			return clss.getPropertyNames();
		}
		
		public Bytecode bytecode() {
			return clss.constructor;
		}
		
		public LeoObject superClass() {
			return clss.superClass;
		}
		
		public LeoArray paramNames() {
			return new LeoArray(clss.paramNames);
		}
	}
	
	/**
	 * Metaclass method
	 */
	private static final Method METACLASS = ClassUtil.getMethodByName(LeoClass.class, "getMetaclass");
	
	private Leola runtime;
	private Bytecode constructor;
	
	private LeoString className;
	private LeoString[] paramNames;
	
	private LeoObject superClass;	
	private Metaclass metaclass;
	
	/**
	 * @param runtime
	 * @param scope
	 * @param superClass
	 * @param className
	 * @param constructor
	 * @param paramNames
	 * @param params
	 */
	public LeoClass(Leola runtime
				  , Scope scope
				  , ClassDefinition classDefinition
				  , LeoObject superClass
				  , LeoObject[] params) {
		super(LeoType.CLASS, scope, classDefinition.getBody().numOuters);
			
		this.runtime = runtime;
		
		this.superClass = superClass;
		this.className = classDefinition.getClassName();
		this.constructor = classDefinition.getBody();
		this.paramNames = classDefinition.getParams();
		this.outers = classDefinition.getOuters();
				
		addProperty(LeoString.valueOf("super"), superClass);
		addProperty(LeoString.valueOf("this"), this);
				
		if ( paramNames != null ) {
			for(int i = 0; i < paramNames.length; i++) {
				if ( params == null || params.length <= i ) {
					addProperty(paramNames[i], LeoNull.LEONULL);
				}
				else {
					addProperty(paramNames[i], params[i]);	
				}
			}
		}
		
		addMethod(this, METACLASS, "metaclass");
		
		this.runtime.execute(this, this.constructor);
		
	}
	
	/**
	 * @return the {@link Metaclass} associated with this instance
	 */
	public Metaclass getMetaclass() {
		if(this.metaclass==null) {
			this.metaclass = new Metaclass(this);
		}
		return this.metaclass;
	}
	
	/**
	 * @return the paramNames
	 */
	public LeoString[] getParamNames() {
		return paramNames;
	}
	
	/**
	 * @return the className
	 */
	public LeoString getClassName() {
		return className;
	}
	
	/**
	 * @return the constructor
	 */
	public Bytecode getConstructor() {
		return constructor;
	}

		
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#isClass()
	 */
	@Override
	public boolean isClass() {
		return true;
	}

	private LeoObject override(LeoString name) {					
		LeoObject function = getProperty(name);
		LeoObject result = this.runtime.execute(function);
		
		return result;
	}
	
	private LeoObject override(LeoString name, LeoObject other) {
		LeoObject function = getProperty(name);
		LeoObject result = this.runtime.execute(function, other);
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#toString()
	 */
	@Override
	public String toString() {
		if ( hasProperty(toString) ) {			
			LeoObject result = override(toString);			
			return result.toString();
		}
		
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		sb.append("{ ");
		LeoMap map = this.getScope().getRawObjects();
		LeoObject[] vars = map.hashKeys;
		LeoObject[] objs = map.hashValues; 
		if ( vars != null ) {
			for(int i = 0; i < vars.length; i++) {
				if(vars[i] != null) {
					if ( !isFirst) {
						sb.append(", ");
					}
					sb.append(vars[i]).append(" : ");
					LeoObject val = objs[i];
					if ( val != null && val != this) {
						if(val.isString()) {
							sb.append("\"").append(val).append("\"");
						}
						else if (val.isScopedObject()) {
							sb.append("<...>");
						}
						else {
							sb.append(val);
						}
					}
					else {
						sb.append("<...>");
					}
					
					isFirst = false;
				}
			}
		}
		sb.append(" }");
		
		return sb.toString();
		
		
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if ( obj instanceof LeoObject ) {
			return this.$eq((LeoObject)obj);
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#isOfType(java.lang.String)
	 */
	@Override
	public boolean isOfType(String rawType) {
		return this.className.toString().equals(rawType) 
			|| ((this.superClass != null)
		 			? this.superClass.isOfType(rawType) : false);
	}
	
		
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#eq(leola.vm.types.LeoObject)
	 */
	@Override
	public boolean $eq(LeoObject other) {
		if ( this == other ) {
			return true;
		}
		
		if ( hasProperty(EQ) ) {			
			LeoObject result = override(EQ, other);			
			return LeoObject.isTrue(result);
		}
		
		
		return super.$eq(other);
	}

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#neq(leola.vm.types.LeoObject)
	 */
	@Override
	public boolean $neq(LeoObject other) {
		if ( hasProperty(NEQ) ) {			
			LeoObject result = override(NEQ, other);			
			return LeoObject.isTrue(result);
		}
		
		return super.$neq(other);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#lte(leola.vm.types.LeoObject)
	 */
	@Override
	public boolean $lte(LeoObject other) {
		if ( hasProperty(LTE) ) {
			LeoObject result = override(LTE, other);
			return LeoObject.isTrue(result);
		}
		return super.$lte(other);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#lt(leola.vm.types.LeoObject)
	 */
	@Override
	public boolean $lt(LeoObject other) {
		
		if ( hasProperty(LT) ) {
			LeoObject result = override(LT, other);
			return LeoObject.isTrue(result);
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#gte(leola.vm.types.LeoObject)
	 */
	@Override
	public boolean $gte(LeoObject other) {
		if ( hasProperty(GTE) ) {
			LeoObject result = override(GTE, other);
			return LeoObject.isTrue(result);
		}
		return super.$gte(other);
	}
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#gt(leola.vm.types.LeoObject)
	 */
	@Override
	public boolean $gt(LeoObject other) {

		if ( hasProperty(GT) ) {
			LeoObject result = override(GT, other);
			return LeoObject.isTrue(result);
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#add(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $add(LeoObject other) {
		if ( hasProperty(ADD) ) {
			LeoObject result = override(ADD, other);
			return result;
		}
		else {
			if (other.isString()) {
				return LeoString.valueOf(toString() + other.toString());
			}
		}
		
		return super.$add(other);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#sub(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $sub(LeoObject other) {
		if ( hasProperty(SUB) ) {
			LeoObject result = override(SUB, other);
			return result;
		}
		return super.$sub(other);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#mul(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $mul(LeoObject other) {
		if ( hasProperty(MUL) ) {
			LeoObject result = override(MUL, other);
			return result;
		}
		return super.$mul(other);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#div(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $div(LeoObject other) {
		if ( hasProperty(DIV) ) {
			LeoObject result = override(DIV, other);
			return result;
		}
		return super.$div(other);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#mod(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $mod(LeoObject other) {
		if ( hasProperty(MOD) ) {
			LeoObject result = override(MOD, other);
			return result;
		}
		return super.$mod(other);
	}

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#neg()
	 */
	@Override
	public LeoObject $neg() {
		if ( hasProperty(NEG) ) {
			LeoObject result = override(NEG);
			return result;
		}
		return super.$neg();
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#bnot()
	 */
	@Override
	public LeoObject $bnot() {
		if ( hasProperty(BNOT) ) {
			LeoObject result = override(BNOT);
			return result;
		}
		return super.$bnot();
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#bsl(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $bsl(LeoObject other) {
		if ( hasProperty(BSL) ) {
			LeoObject result = override(BSL, other);
			return result;
		}
		return super.$bsl(other);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#bsr(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $bsr(LeoObject other) {
		if ( hasProperty(BSR) ) {
			LeoObject result = override(BSR, other);
			return result;
		}
		return super.$bsr(other);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#land(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $band(LeoObject other) {
		if ( hasProperty(BAND) ) {
			LeoObject result = override(BAND, other);
			return result;
		}
		return super.$band(other);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#lor(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $bor(LeoObject other) {
		if ( hasProperty(BOR) ) {
			LeoObject result = override(BOR, other);
			return result;
		}
		return super.$bor(other);
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#xor(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $xor(LeoObject other) {
		if ( hasProperty(XOR) ) {
			LeoObject result = override(XOR, other);
			return result;
		}
		return super.$xor(other);
	}
	
		
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#getValue()
	 */
	@Override
	public Object getValue() {
		return this;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#clone()
	 */
	@Override
	public LeoObject clone() {
		return null;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.write(this.getType().ordinal());
		this.className.write(out);
		this.constructor.write(out);
	}
	
	/**
	 * Reads from the {@link DataInput} stream, constructing a {@link LeoObject}
	 * 
	 * @param in
	 * @return the {@link LeoObject}
	 * @throws IOException
	 */
	public static LeoClass read(DataInput in) throws IOException {
		return null; /* TODO */
	}
}


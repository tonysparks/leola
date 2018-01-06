Leola Programming Language
=====


The Leola Programming Language was created on April 1st, 2010.  It wasn't created out of frustration with existing languages nor for a niche requirement.  It was simply created because I find programming languages and compilers interesting.  
  							
If you are looking for an expressive, performant and/or robust programming language, chances are you will be more than happy with other more mature languages such as Python, Ruby or Lua.  If you just want to try and learn a new programming language for fun, please continue on reading!
								
My main goal for Leola is to scratch my itch for learning more about compilers and language theory.  In addition, I also like to use it for my personal use -- therefore I have a vested interest in making it performant, stable and feature rich.
		  					
Leola is a dynamically typed language which supports Object Orientated Programming, Data Driven Programming and a splash of Functional Programming.  It can be embedded into a Java program or can be executed as a stand alone application.  It is written in the Java programming language and is compiled to Leola bytecode.  Yes, as it stands this is a VM on top of a VM.  A C VM implementation is planned, but currently not available.

Features
=====
Leola currently supports these features:
* classes
* single inheritance
* namespaces
* generators
* language support for expandable arrays
* language support for maps
* easily embeddable in Java programs
* easily access Java types in Leola code
* closures
* higher order functions
* tailcail optimization
* named parameters
* decorators

Sample Code
====

Defining some variables:
````javascript
var aString = "Hello World"
var aNumber = 1024
var anArray = [ aString, aNumber, 45 ]

// defining a Map
var aMap = {
   hello -> "Hola!",
   bye -> "Adios",
}
println( aMap.bye ) // prints Adios

// defining a function
var square = def(x) return x*x
var v = square(2)
println(v) // prints 4

// a simple class
class Person(firstName, lastName, age);
var aLegend = new Person("Brett", "Favre", 41)
println( aLegend.lastName + " is " + aLegend.age + " years old.") // prints Favre is 41 years old.

// a namespace
namespace nfl {
   
   var getTheBestTeamEver = def() {
      return new nfl:Team("Green Bay", "Packers")   
   }
   
   class Team(city, name) {      
      var getFullName = def() return city + " " + name
   }
}

var worseTeamEver = new nfl:Team("Chicago", "Bears")
var bestTeamEver = nfl:getTheBestTeamEver()


println("The " + worseTeamEver.getFullName() + " are the worst.")
println("The " + bestTeamEver.getFullName() + " are the best.")

/* named parameters are also supported */
var bestQB = def(from, to) {
  if from > 0 && to < 1991 {
    return "Bart Star"
  }
  
  /* who cares about the year, it will always be Brett */
  return "Brett Favre"
}

// use named parameters by nameOfParameter->Value
println("The best QB of all time: " + bestQB(from->1970, to->2015))

````

How about some calculus?

````javascript
/* the derivative function */
var derivative = def(f) {
   var dx = 0.0000001
   
   var fPrime = def(x) return (f(x + dx) - f(x)) / dx
   
   return fPrime
}

// take the derivative of a simple function x^3
var cube = def(x) return x*x*x   // x^3
var dCube = def(x) return derivative(cube)(x)  // should be roughly 3x^2


println( dCube(4.0) ) // prints 48.00000141358396
println( dCube(10.0) ) // prints 300.0000003794412

// how about the derivative of sin(x)
import("java.lang.Math") 
var dSin = def(x) return derivative(sin)(x)  // should be roughly cos(x)
println( dSin(1.2) ) // prints 0.36235770828341174

````

Statements and Expressions

````javascript

/* if statement */
var x = 10
if x < 20 {
   println("x < 20")
}
else {
   println("x >= 20")
}

/* while statement */
while x < 20 {
   if x == 15 break    /* breaks out of the loop */   
   x += 1
}


/* a switch statement */
var color = "red"
switch color {
   when "red" -> println("A red truck")
   when "blue" -> println("A blue whale")
   else println("The night sky")
} // prints A red truck

/* a case expression is similar to a switch, but it's an expression */
var text = case color {
   when "red" -> "A red truck"
   when "blue" -> "A blue whale"
   else "The night sky"
}

println(text) // prints A red truck

/* is expression */
println(color is String) // prints true


/* define a function with variable amount of arguments */
var sum = def(elements ...) {
  result = 0
  
  /* elements is an array */
  foreach(elements, def(e) result += e)
  return result
}

var total = sum(1,53,2)
println(total) // 56

/* Use the 'explode' operator (*) to explode out an array to fill in function
   arguments */
var sub = def(a, b) {
    return a - b
}
// The * will place make a=3, b=1
println(sub(*[3, 1]) )   // prints 2

````

Exception Handling
=====
Leola uses Exceptions much like Javascript, Java, etc.

````javascript
try {
  throw "Some Error"
}
catch e {
  println(e) // prints 'Some Error'
}
````

Generators
=====

Generators in Leola are similar to the generators in Python, with some slight differences.  Mainly, the ability to pass parameters to them with each invocation.  Any other local variables of the generator are stored and retained in the generator (the exception being the parameters).  

````javascript
/* define a generator */
var count = gen(n) {
  var i = 0
  while i < n {
    yield i
    i+=1
  }
}

while true {
  var i = count(10)
  
  /* an exhausted generator returns null */
  if i!=null {
    print(i + " ")
  }
  else break
}

/* this will print: 0 1 2 3 4 5 6 7 8 9 */

````

Decorators
=====

Decorators in Leola are very similar to decorators in Python (I know I'm original).  Basically, what they are is a function that wraps another function.  Why is this useful? It allows for some interesting constructs such as:

````javascript
// Type check the arguments past to the function
// are of the correct type
var TypeCheck = def(func, args...) {    
    return def(nargs...) {
        var i = 0
        foreach(nargs, def(arg) {
            if i < args.size() {
                if reflect:type(arg).toLower() != args[i].toLower() {
                    throw arg + "(" + reflect:type(arg) + ") is not of type '" + args[i] + "'"
                }
            }
        
            i += 1
        })
        
        // if all the types are OK, go
        // ahead and execute the function
        return func(*nargs)
    }
}



var sub = @TypeCheck("integer", "integer") def(a, b) {
    return a - b
}

println(sub(4,5) ) // -1
println(sub("x", 5) ) // throws an error

````

Interfacing with Java
=====

Easily create libraries written in Java to call from Leola code.  This allows programmers to put performance
critical code in Java and call those functions from Leola.

````java
package test;

import leola.vm.Leola;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;

import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoArray;

/* As a hint to the Leola runtime, by convention
   Java leola libraries should be named as XLeolaLibrary, where
   X is the name of your library.
*/
public class MyFirstLeolaLibrary implements LeolaLibrary {

   private Leola runtime;

   @Override
   @LeolaIgnore // does not import this method in Leola
   public void init(Leola runtime, LeoNamespace namespace) throws Exception {
      this.runtime = runtime;
      
      // places all of this classes
      // public methods into the supplied namespace (with the exception of
      // the @LeolaIgnore methods)
      this.runtime.putIntoNamespace( this, namespace); 
   }
   
   public int add(int left, int right) {
      return left + right
   }
   
   public void map(LeoArray input, LeoObject functor) {
      int size = input.size();
      for(int i = 0; i < size; i++) {
         // execute the supplied functor object
         LeoObject newValue = this.runtime.execute(functor, input.get(i));
         
         // set the value back in the array
         input.set(i, newValue);
      }
   }
}
````

Now in Leola we can use it as so:
````javascript
// after we compile and jar up our java code (say we name the jar file 'myFirst.jar'), we
// reference it here to import it.  Now it is stored in the "first" namespace
require("lib/myFirst.jar", "first" )

var sum = first:add( 2, 2 )
println(sum) // prints 4

var odds = [1,3,5] 
first:map( odds, def(e) return e+1 )

println(odds) // prints [2,4,6]


````

We can also load it to the global namespace by omitting the second parameter on the 'require' call.
````javascript
require("lib/myFirst.jar")

var sum = add( 2, 2 )
println(sum) // prints 4

var odds = [1,3,5] 
map( odds, def(e) return e+1 )

println(odds) // prints [2,4,6]
````

You can also instantiate any Java object on the Leola classpath.  The LeolaLibrary option lets you
write API's tailored towards Leola.

````javascript
var jMap = new java.lang.HashMap()
jMap.put("winners", "Packers")
var winners = jMap.get("winners")
println(winners) // prints Packers

````


How to run
=====

````
java -jar leola.jar "./your_script.leola"
````

Maven
=====

You can download the latest version of Leola by using these Maven coordinates:
```
<dependency>
  <groupId>leola</groupId>
  <artifactId>leola</artifactId>
  <version>0.9.6</version>
</dependency>
```

More to come..

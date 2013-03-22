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
* language support for expandable arrays
* language support for maps
* easily embeddable in Java programs
* easily access Java types in Leola code
* closures
* higher order functions
* tailcail optimization

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


````

How to run
=====

````
java -jar leola.jar "your_script.leola"
````

More to come..

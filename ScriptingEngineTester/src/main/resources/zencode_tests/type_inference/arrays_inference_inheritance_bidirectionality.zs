#output: 2
#output: 2

virtual class Car {

}

class ElectricCar : Car {

}

val c = new Car();
val ec = new ElectricCar();

val firstArray = [
	c,
	ec
];

val secondArray = [
	ec,
	c
];

println(firstArray.length);
println(secondArray.length);
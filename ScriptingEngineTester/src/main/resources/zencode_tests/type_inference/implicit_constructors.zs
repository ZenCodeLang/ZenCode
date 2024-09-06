#output: {"hello": ["world", 123, {"great": true, "greater": "yes"}]}

// Of course, this would need to also escape the " symbol in the string,
//   but should be enough for here
function enquote(input: string): string => '"' + input + '"';


interface JsonData {

	public implicit this(data: JsonData[]) {
	    // this one uses the method body syntax to verify that both syntaxes work
	    return new JsonArray(data);
	}
	public implicit this(data: JsonData[string]) => new JsonObject(data);
	public implicit this(data: string)  => new JsonString(data);
	public implicit this(data: int)  => new JsonNumber(data);
	public implicit this(data: bool) => new JsonBool(data);

	// ToDo: this the proper way?
	//public implicit this(data: null) => new JsonNull();

	public as string;
}

class JsonArray {

	var data as JsonData[] : get, protected set;
	public this(data: JsonData[]){ this.data = data; }

	 implements JsonData{
		public as string {
			var build = "[";
			var first = true;

			for value in this.data {
				if first {
					first = false;
				} else {
					build += ", ";
				}

				build += (value as string);
			}

			return build + ']';
		}
	}
}

class JsonObject {

	var data as JsonData[string] : get, protected set;
	public this(data: JsonData[string]){ this.data = data; }

	implements JsonData{
		public as string {
			var build = "{";
			var first = true;

			for key, value in this.data {
				if first {
					first = false;
				} else {
					build += ", ";
				}


				build += enquote(key);
				build += ': ';
				build += (value as string);
			}

			return build + '}';
		}
	}
}

class JsonString {

	var data as string : get, protected set;
	public this(data: string) { this.data = data; }

	implements JsonData{
		public as string => enquote(this.data);
	}
}

class JsonNumber {

	var data as int : get, protected set;
	public this(data: int) { this.data = data; }

	implements JsonData{
		public as string => this.data as string;
	}
}

class JsonBool {

	var data as bool : get, protected set;
	public this(data: bool){ this.data = data; }

	implements JsonData{
		public as string => (this.data as string);
	}
}

class JsonNull {
	public this(){}

	implements JsonData {
		public as string => 'null';
	}
}

var x: JsonData = {
	"hello": ["world", 123, {"great": true, "greater": "yes"}]
};

println(x as string);

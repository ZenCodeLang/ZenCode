#output: {"hello": ["world", 123, {"great": true}]}

function enquote(input: string): string { return '"' + input + '"'; }


interface JsonData {

    public implicit this(data: JsonData[]) => new JsonArray(data);
    public implicit this(data: JsonData[string]) => new JsonObject(data);
    public implicit this(data: string)  => new JsonString(data);
    public implicit this(data: bool) => new JsonBool(data);

    // ToDo: this the proper way?
    public implicit this(data: null) => new JsonNull();

    public as string;
}

class JsonArray {

    var data as JsonData[] : get, protected set;
    public this(data: JsonData[]){ this.data = data; }

    implements JsonData{
        public as string {
            var build = "{";
            var first = true;

            for key, value in this.data {
                if first {
                    first = false;
                } else {
                    build += ",\n";
                }


				build += enquote(key);
				build += ':';
                build += (value as string);
            }

            return build + '}';
        }
    }
}

class JsonObject {

    var data as JsonData[string] : get, protected set;
    public this(data: JsonData[string]){ this.data = data; }

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

class JsonString {

    var data as string : get, protected set;
    public this(data: string) { this.data = data; }

    implements JsonData{
        public as string => enquote(this.data);
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

    implements JsonData{
            public as string => 'null';
        }
}

var x: JsonData = {
    "hello": ["world", 123, {"great": true}]
};
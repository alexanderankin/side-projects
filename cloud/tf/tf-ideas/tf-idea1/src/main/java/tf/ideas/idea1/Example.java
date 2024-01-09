package tf.ideas.idea1;

import static tf.ideas.idea1.Configuration.Property.stringProperty;
import static tf.ideas.idea1.Configuration.Variable.variable;

public class Example {
    /*
        variable "var1" { default = "abc" }
        variable "var2" { default = "def" }

        resource example_resource my_test { name = var.var1 }

        output "var1" { value = var.var1 }
        output "var2" { value = var.var2 }
     */
    public static void main(String[] args) {
        new Configuration()
                .add(variable("var1").setType(VariableType.string()).setDefaultValue("abc"))
                .add(variable("var2").setType(VariableType.string()).setDefaultValue("def"))
                .add(new Configuration.Resource()
                        .setResourceType("example_resource")
                        .setName("my_test")
                        .add(stringProperty("name", "var.var1")))
                .add("output \"var1\" { value = var.var1 }")
                .add("output \"var2\" { value = var.var2 }")
                .add(new Configuration.Resource());
    }
}

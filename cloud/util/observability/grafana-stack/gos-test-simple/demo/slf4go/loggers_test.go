package slf4go_test

import (
	"fmt"
	"strings"
	"testing"
)

func TestLoggers(t *testing.T) {
	parts := strings.Split("abc.def.xyz.123", ".")
	for i := len(parts) - 1; i >= 0; i-- {
		fmt.Println(strings.Join(parts[:i], "."))
	}
}

type Status int

const (
	Pending  Status = iota // 0
	Approved               // 1
	Rejected               // 2
)

func TestThings(t *testing.T) {
	var rawValue int = 4
	var myStatus Status = Status(rawValue) // Casting the int to Status

	fmt.Println(myStatus) // Output: 1 (representing Approved)

	//var anotherRawValue string = "Rejected"
	//fmt.Println(anotherRawValue)
	// Direct casting of a string to an int-based enum is not possible without a conversion function.
	// You would need a function to map string to Status value.
	// var stringStatus Status = Status(anotherRawValue) // This would not compile
}

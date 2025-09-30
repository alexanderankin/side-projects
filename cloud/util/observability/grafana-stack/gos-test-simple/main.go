package main

import (
	"fmt"
	"gos-test-simple/demo"

	"github.com/spf13/cobra"
)

func forceGet[T any](t T, err error) T {
	if err != nil {
		panic(err)
	}
	return t
}

func main() {
	rootCmd := &cobra.Command{Use: "gos-test-simple", Short: "A demo CLI application", Version: "1.0.0"}
	rootCmd.SetVersionTemplate(fmt.Sprintf("%s\n", rootCmd.Version))

	demoCmd := &cobra.Command{
		Use:   "demo",
		Short: "Run the demo",
		RunE: func(cmd *cobra.Command, _ []string) error {
			mode := forceGet(cmd.Flags().GetString("mode"))
			local := forceGet(cmd.Flags().GetBool("use-localhost"))
			return demo.Demo(map[string]demo.Mode{"stdout": demo.Stdout, "otel": demo.OTel}[mode], local)
		},
	}
	demoCmd.Flags().StringP("mode", "m", "stdout", "Mode to run (stdout|otel)")
	demoCmd.Flags().BoolP("use-localhost", "l", true, "use localhost alloy? if false, require otel endpoint env vars")
	rootCmd.AddCommand(demoCmd)

	// local debugging
	rootCmd.SetArgs([]string{"demo", "--mode", "otel", "--use-localhost"})

	// Execute
	err := rootCmd.Execute()
	if err != nil {
		panic(err)
	}
}

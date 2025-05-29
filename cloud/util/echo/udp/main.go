package main

import (
	"fmt"
	"net"
	"os"
)

func main() {
	port := os.Getenv("PORT")
	if port == "" {
		port = "9000"
	}
	addr := ":" + port

	conn, err := net.ListenPacket("udp", addr)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error listening on port %s: %v\n", port, err)
		os.Exit(1)
	}
	defer conn.Close()
	fmt.Printf("UDP echo server listening on %s\n", addr)

	buf := make([]byte, 2048)

	for {
		n, remoteAddr, err := conn.ReadFrom(buf)
		if err != nil {
			fmt.Fprintf(os.Stderr, "Read error: %v\n", err)
			continue
		}

		_, err = conn.WriteTo(buf[:n], remoteAddr)
		if err != nil {
			fmt.Fprintf(os.Stderr, "Write error: %v\n", err)
		}
	}
}

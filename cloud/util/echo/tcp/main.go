package main

import (
	"bufio"
	"io"
	"log"
	"net"
	"os"
)

const maxConcurrentConnections = 100

var sem = make(chan struct{}, maxConcurrentConnections)

func main() {
	port := os.Getenv("PORT")
	if port == "" {
		port = "9000"
	}

	listener, err := net.Listen("tcp", ":"+port)
	if err != nil {
		log.Fatalf("Error starting TCP server: %v", err)
	}
	defer func(listener net.Listener) {
		_ = listener.Close()
	}(listener)

	log.Printf("TCP Echo server listening on port %s", port)

	for {
		conn, err := listener.Accept()
		if err != nil {
			log.Printf("Failed to accept connection: %v", err)
			continue
		}

		select {
		case sem <- struct{}{}: // acquire slot
			go func(c net.Conn) {
				defer func() { <-sem }() // release slot
				handleConnection(c)
			}(conn)
		default:
			log.Printf("Connection from %s rejected: too many concurrent connections", conn.RemoteAddr())
			_, _ = conn.Write([]byte("Server busy. Try again later.\n"))
			_ = conn.Close()
		}
	}
}

func handleConnection(conn net.Conn) {
	defer func(conn net.Conn) {
		err := conn.Close()
		if err != nil {
			log.Printf("Failed to close a connection %s: %v", conn.RemoteAddr(), err)
		}
	}(conn)
	log.Printf("Client connected from %s", conn.RemoteAddr())

	reader := bufio.NewReader(conn)
	for {
		data, err := reader.ReadBytes('\n')
		if err != nil {
			if err != io.EOF {
				log.Printf("Error reading: %v", err)
			}
			break
		}

		// Echo back to the client
		_, err = conn.Write(data)
		if err != nil {
			log.Printf("Error writing: %v", err)
			break
		}
	}
	log.Printf("Client disconnected from %s", conn.RemoteAddr())
}

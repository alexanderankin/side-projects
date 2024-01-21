what if you had two tcp services talking to each other, and you wanted to snoop on the contents.

this folder should be used to describe a tcp proxy solution which logs to disk

for example, you may serve http traffic on port 8080:

npx http-server -p 8080

then you may proxy traffic via port 8081

socat TCP-LISTEN:8081,fork,reuseaddr TCP:127.0.0.1:8080

then you can curl on port 8081:

curl localhost:8081

but how about persisting that traffic to disk? (or sending it anywhere else)

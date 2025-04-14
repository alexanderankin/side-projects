import { app } from "./app.js";

const port = process.env.PORT && Number.parseInt(process.env.PORT, 10) || 3000;

app.listen(port, "127.0.0.1", 512, function () {
  console.log("hello");
});

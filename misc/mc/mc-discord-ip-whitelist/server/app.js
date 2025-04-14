import express from "express";
import path from "path";
import cookieParser from "cookie-parser";
import logger from "morgan";

import { router as indexRouter } from "./routes/index.js";
// const usersRouter = require('./routes/users');

export const app = express();

app.use(logger(process.env.NODE_ENV === "production" ? "default" : "dev"));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
const __dirname = import.meta.dirname
console.log(__dirname);
app.use(express.static(path.join(__dirname, "public")));

app.use("/", indexRouter);
// app.use('/users', usersRouter);

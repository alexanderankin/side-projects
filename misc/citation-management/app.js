import express from "express";
import 'express-async-errors'
import path from "node:path";
import cookieParser from "cookie-parser";
import logger from "morgan";
import indexRouter from "./routes/index.js";
import usersRouter from "./routes/users.js";
import knex from 'knex';
import knexConfig from './knexfile.js';

const __dirname = path.dirname(import.meta.url.substring('file://'.length));
const app = express();

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'dist')));
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);
app.use('/users', usersRouter);

/** @type import('express').ErrorRequestHandler */
function errors(err, req, res, next) {
  res.status(500).send({
    err: err.message
  })
}

app.use(errors);

export default app;

export async function getApp() {
  const db = knex(knexConfig[process.env.NODE_ENV || 'development']);
  await db.migrate.latest();
  app.locals.db = db;
  return app;
}

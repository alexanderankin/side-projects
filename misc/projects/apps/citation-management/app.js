import express from "express";
import 'express-async-errors'
import path from "node:path";
import url from 'node:url';
import cookieParser from "cookie-parser";
import logger from "morgan";
import createError from "http-errors";
import indexRouter from "./routes/index.js";
import usersRouter from "./routes/users.js";
import knex from 'knex';

const __dirname = path.dirname(url.fileURLToPath(import.meta.url));
const app = express();

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'dist')));
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);
app.use('/users', usersRouter);

// noinspection JSUnusedLocalSymbols
/** @type import('express').ErrorRequestHandler */
function errors(err, req, res, next) {
  res.status(err.status || 500).send({
    err: err.message
  })
}

app.use('/api', (r, s, n) => n(createError(404)));
app.use((r, s) => s.sendFile('index.html', { root: path.join(__dirname, 'dist') }));
app.use(errors);

export default app;

export async function getApp() {
  let { default: knexConfig } = await import('./knexfile.js')
  const db = knex(knexConfig[process.env.NODE_ENV || 'development']);
  await db.migrate.latest();
  app.locals.db = db;
  return app;
}

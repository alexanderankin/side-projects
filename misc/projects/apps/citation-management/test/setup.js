import debug from "debug";
import { PostgreSqlContainer } from "@testcontainers/postgresql";

import { getApp } from "../app.js";

const logger = debug('citation-management:itest:setup')

let postgres = new PostgreSqlContainer('postgres:16-alpine')
  .withDatabase('citations')
;

let started;

beforeAll(async () => {
  if (!started) {
    if (process.env.TEST_DB === 'sqlite3') {
      started = true;
      process.env.NODE_ENV = 'development';
      process.env.SQLITE_FILENAME = ':memory:';
    } else {
      logger('not started yet, starting');
      started = await postgres.start();
      logger('postgres started');
      process.env.NODE_ENV = 'production';
      process.env.POSTGRES_USER = started.getUsername();
      process.env.POSTGRES_PASSWORD = started.getPassword();
      process.env.POSTGRES_PORT = String(started.getPort());
    }

    let app = await getApp();
    logger('app loaded');
    let server = app.listen();
    process.env.PORT = String(server.address().port);
    logger('app listening', server.address());
  }
});

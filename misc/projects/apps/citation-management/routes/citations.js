import express from "express";
import lodash from "lodash";
import debug from 'debug';

let router = express.Router();
let logger = debug('citation-management:api:citations');

router.use((req, res, next) => {
  // logger('hello');
  next();
});

/* GET home page. */
router.get('/api/citations', async function (req, res) {
  /** @type import('knex').Knex */
  let db = res.app.locals.db;
  let query = db('citation')
    .orderBy('id', 'desc')
    .limit(parseInt(req.query.limit || '10', 10))
    .offset(parseInt(req.query.offset || '0', 10));

  if (req.query.search) {
    query.where('name', 'like', '%' + req.query.search + '%');
  }

  let rows = await query;
  res.send(rows);
});

router.post('/api/citations', async function (req, res) {
  /** @type import('knex').Knex */
  let db = res.app.locals.db;
  let now = new Date();
  let data = {
    ...lodash.pick(req.body, ['name']),
    created_at: now,
    updated_at: now,
  };
  let [id] = await db('citation').insert(data).returning('id');
  if (id.id) id = id.id; // postgres
  res.status(201).send({ ...data, id });
});

router.get('/api/citations/:id', async function (req, res) {
  /** @type import('knex').Knex */
  let db = res.app.locals.db;
  res.send(await db('citation').where({ id: req.params.id }).first());
});

router.put('/api/citations/:id', async function (req, res) {
  /** @type import('knex').Knex */
  let db = res.app.locals.db;
  let now = new Date();
  let data = {
    ...lodash.omit(req.body, ['id']),
    updated_at: now,
  };
  if (!(await db('citation').where({ id: req.params.id }).update(data)))
    return res.status(404).end();
  res.send(await db('citation').where({ id: req.params.id }).first());
});

router.delete('/api/citations/:id', async function (req, res) {
  /** @type import('knex').Knex */
  let db = res.app.locals.db;
  let value;
  await db.transaction(async trx => {
    value = await trx('citation').where({ id: req.params.id }).first();
    if (!value) return res.status(404);
    await trx('citation').where({ id: req.params.id }).del();
  });
  res.send(value);
});

router.get('/api/citations/:id/cites', async (req, res) => {
  /** @type import('knex').Knex */
  let db = res.app.locals.db;
  let rows = await db('citation_citation')
    .select('citation.*')
    .leftJoin('citation', 'citation_to', 'id')
    .where({ citation_from: req.params.id })
    .limit(parseInt(req.query.limit || '10', 10))
    .offset(parseInt(req.query.offset || '0', 10))
    .orderBy('created_at', 'desc')
  res.send(rows);
});

/**
 * PUT into the collection of citations where id cites toId
 */
router.put('/api/citations/:id/cites/:toId', async (req, res) => {
  /** @type import('knex').Knex */
  let db = res.app.locals.db;
  let data = {
    citation_from: req.params.id,
    citation_to: req.params.toId,
    created_at: new Date(),
  };
  if (data.citation_from === data.citation_to) return res.status(400).send({ error: 'no self' });
  await db('citation_citation').insert(data);
  res.send(data);
})

router.get('/api/citations/:id/cited', async (req, res) => {
  /** @type import('knex').Knex */
  let db = res.app.locals.db;
  let rows = await db('citation_citation')
    .select('citation.*')
    .leftJoin('citation', 'citation_from', 'id')
    .where({ citation_to: req.params.id })
    .limit(parseInt(req.query.limit || '10', 10))
    .offset(parseInt(req.query.offset || '0', 10))
    .orderBy('created_at', 'desc')
  res.send(rows);
});

router.delete('/api/citations/:id/cites/:toId', async (req, res) => {
  /** @type import('knex').Knex */
  let db = res.app.locals.db;
  console.log(await db('citation_citation').del().where({ citation_from: req.params.id, citation_to: req.params.toId }))
  res.status(200).end();
});

/**
 * PUT into the collection of citations where id is cited by fromId
 */
router.put('/api/citations/:id/cited/:fromId', async (req, res) => {
  /** @type import('knex').Knex */
  let db = res.app.locals.db;
  let data = {
    citation_from: req.params.fromId,
    citation_to: req.params.id,
    created_at: new Date(),
  };
  if (data.citation_from === data.citation_to) return res.status(400).send({ error: 'no self' });
  await db('citation_citation').insert(data);
  res.send(data);
});

router.get('/api/latest-citations', async (req, res) => {
  /** @type import('knex').Knex */
  let db = res.app.locals.db;
  let rows = await db({ cc: 'citation_citation' })
    .select([
      'from.id as from_id',
      'from.name as from_name',
      'to.id as to_id',
      'to.name as to_name',
      'cc.created_at',
    ])
    .orderBy('cc.created_at', 'desc')
    .leftJoin({ from: 'citation' }, 'cc.citation_from', 'from.id')
    .leftJoin({ to: 'citation' }, 'cc.citation_to', 'to.id')
    .limit(parseInt(req.query.limit || '10', 10))
    .offset(parseInt(req.query.offset || '0', 10))
  let data = rows.map(d => ({
    createdAt: d.created_at,
    from: {
      id: d['from_id'],
      name: d['from_name'],
    },
    to: {
      id: d['to_id'],
      name: d['to_name'],
    }
  }))
  res.send(data);
});

router.delete('/api/citations/:id/cited/:fromId', async (req, res) => {
  /** @type import('knex').Knex */
  let db = res.app.locals.db;
  console.log(await db('citation_citation').del().where({ citation_to: req.params.id, citation_from: req.params.fromId }))
  res.status(200).end();
});

export default router;

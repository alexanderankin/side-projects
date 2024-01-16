import './setup.js'
import { expect } from "chai";

describe('citation tests', () => {
  it('citation crud', async () => {
    let response, body;
    body = await fetch(`http://localhost:${process.env.PORT}/api/citations`)
      .then(r => r.json());
    expect(body).to.be.an('array');

    response = await fetch(`http://localhost:${process.env.PORT}/api/citations`, {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ name: 'citation-crud.1' })
    });
    expect(response.ok).to.be.true
    expect(response.status).to.equal(201)
    body = await response.json();
    expect(body.name).to.equal('citation-crud.1');
    expect(body.id).to.be.ok
    expect(body.created_at).to.be.ok
    expect(body.description).to.not.be.ok

    for (let i = 0; i < 10; i++) {
      expect((await fetch(`http://localhost:${process.env.PORT}/api/citations`, {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({ name: 'citation-crud.i.' + i })
      })).status).to.equal(201);
    }

    response = await fetch(`http://localhost:${process.env.PORT}/api/citations`);
    body = await response.json();
    expect(body).to.have.length(10);
    let last = body[0]
    let semiLast = body[1]

    response = await fetch(`http://localhost:${process.env.PORT}/api/citations/` + last.id, {
      method: 'PUT',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ name: 'citation-crud.i.changed' })
    });
    expect(response.ok).to.be.ok
    body = await response.json();
    expect(body).to.be.ok
    expect(body.name).to.equal('citation-crud.i.changed');
    expect(body.created_at).to.be.ok;
    expect(body.updated_at).to.be.ok;
    expect(body.created_at).to.not.equal(body.updated_at);

    response = await fetch(`http://localhost:${process.env.PORT}/api/citations/` + last.id, {
      method: 'DELETE',
    });
    expect(response.ok).to.be.ok
    expect(body.name).to.equal('citation-crud.i.changed');
    body = await response.json();

    response = await fetch(`http://localhost:${process.env.PORT}/api/citations/` + last.id, {
      method: 'DELETE',
    });
    expect(response.ok).to.not.be.ok

    response = await fetch(`http://localhost:${process.env.PORT}/api/citations`);
    body = await response.json();
    expect(body).to.have.length(10);
    let newLast = body[0]
    expect(semiLast.id).to.equal(newLast.id);
  });

  it('citation cites and cited', async () => {
    let ids = []
    for (let i = 0; i < 3; i++) {
      let response = await fetch(`http://localhost:${process.env.PORT}/api/citations`, {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({ name: 'citation-cites-cited.' + i })
      });
      expect(response.status).to.equal(201);
      let { id } = await response.json();
      ids.push(id);
    }

    let cites0 = await fetch(`http://localhost:${process.env.PORT}/api/citations/${ids[0]}/cites`).then(r => r.json());
    expect(cites0).to.deep.equal([]);

    let cited0 = await fetch(`http://localhost:${process.env.PORT}/api/citations/${ids[1]}/cited`).then(r => r.json());
    expect(cited0).to.deep.equal([]);

    let response = await fetch(`http://localhost:${process.env.PORT}/api/citations/${ids[0]}/cites/${ids[1]}`, {
      method: 'PUT'
    });
    expect(response.ok).to.be.ok;

    cites0 = await fetch(`http://localhost:${process.env.PORT}/api/citations/${ids[0]}/cites`).then(r => r.json());
    expect(cites0).to.have.length(1);
    expect(cites0[0]).to.include({ id: ids[1] })
    cited0 = await fetch(`http://localhost:${process.env.PORT}/api/citations/${ids[1]}/cited`).then(r => r.json());
    expect(cited0).to.have.length(1);
    expect(cited0[0]).to.include({ id: ids[0] })
  });
});

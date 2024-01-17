/*
  View to show a single article ("citation")
*/
import { Link, useParams } from "react-router-dom";
import { useMutation, useQuery, UseQueryResult } from "react-query";
import { formatDate } from "./dateFormatUtils";
import { LoadingListGroup, okResponse } from "./reusable";
import { Citation } from "./models";
import { Form } from "react-bootstrap";
import { useState } from "react";
import lodash from "lodash";

function AddLinks({ citation, queries }: { citation: Citation, queries: Array<UseQueryResult<any, any>> }) {
  const [selected, setSelected] = useState<Citation>()
  const [search, setSearch] = useState<string>()

  let searchQuery = useQuery('AddLinks.search', async () => {
    console.log('searching for', search);
    if (!search?.length) return [];
    return fetch('/api/citations?search=' + search).then(okResponse)
      .then(r => r.json())
      .then(b => b as Array<Citation>);
  }, { cacheTime: -1 });

  let citesMutation = useMutation(() => {
    return fetch(`/api/citations/${citation.id}/cites/${selected.id}`, { method: 'PUT' }).then(() => queries.forEach(q => q.refetch()));
  });

  let citedMutation = useMutation(() => {
    return fetch(`/api/citations/${citation.id}/cited/${selected.id}`, { method: 'PUT' }).then(() => queries.forEach(q => q.refetch()));
  });

  return <>
    <h2>Add link from {citation.name} to:</h2>
    <div className='small text-muted'>Search by name:</div>
    <Form.Control
      required
      type='text'
      name='name'
      onChange={lodash.debounce(e => (setSearch(e.target.value), searchQuery.refetch()), 50)}
      placeholder='Article title'
      disabled={!!selected}
      // defaultValue='Mark'
    />

    {/*
    <Form validated onSubmit={e => {
      e.preventDefault();
    }}>
      <Row className='mb-3'>
        <Form.Group as={Col} md='6' controlId='validationCustom01'>
          <Form.Control
            required
            type='text'
            name='name'
            placeholder='Article title'
            // defaultValue='Mark'
          />
          <Form.Control.Feedback>Looks good!</Form.Control.Feedback>
        </Form.Group>
        <Form.Group as={Col} md='6' controlId='validationCustom02'>
          <Form.Label>Description</Form.Label>
          <Form.Control
            // required
            type='text'
            name='description'
            placeholder='Note about the article (optional)'
            // defaultValue='Otto'
          />
          <Form.Control.Feedback>Looks good!</Form.Control.Feedback>
        </Form.Group>
      </Row>
      <Button variant='outline-success' type='submit'>Create</Button>
    </Form>
     */}

    {!!selected
      ? <>
        <div className="list-group mt-2" onClick={() => setSelected(null)}>
          <div className="list-group-item list-group-item-action btn btn-outline-danger">
            <span className='small text-muted'>Click to unselect:</span>
            {' '}
            <CitationResultItem citation={selected} />
          </div>
        </div>
      </>
      : <div className="list-group mt-2">
        {/*
      <div key={"1"} className="list-group-item list-group-item-action">
        <CitationResultItem
          citation={{ id: 1, name: 'abc', created_at: '1', updated_at: '1' }} />
      </div>
      <div key={"2"} className="list-group-item list-group-item-action">
        <CitationResultItem
          citation={{ id: 1, name: 'abc', created_at: '1', updated_at: '1' }} />
      </div>
       */}
        <LoadingListGroup result={searchQuery} keyFn={k => k.id} nodeFn={e => <>
          <div onClick={() => setSelected(e)}>
            <CitationResultItem citation={e} />
          </div>
        </>} />
      </div>
    }

    {!selected
      ? null
      : <>
        <div className='my-3'>choose direction:</div>
        <div>
          <button
            className='btn btn-outline-primary'
            role='button'
            disabled={citesMutation.isSuccess}
            onClick={() => citesMutation.mutate()}
          >
            Citing:
            {!citesMutation.isLoading ? null : <span>loading...</span>}
            {!citesMutation.isError ? null :
              <span>Error: {String(citesMutation.error)}</span>}
            {!citesMutation.isSuccess ? null : <span>linked!</span>}
          </button>
        </div>
        <div>
          <button
            className='btn btn-outline-primary' role='button'
            disabled={citedMutation.isSuccess}
            onClick={() => citedMutation.mutate()}
          >
            Cited by:
            {!citedMutation.isLoading ? null : <span>loading...</span>}
            {!citedMutation.isError ? null :
              <span>Error: {String(citedMutation.error)}</span>}
            {!citedMutation.isSuccess ? null : <span>linked!</span>}

          </button>
        </div>
      </>
    }
  </>
}

export function CitationPage() {
  let { citationId } = useParams();

  let citationQuery = useQuery(`citation.${citationId}`, async () =>
    fetch(`/api/citations/${citationId}`).then(okResponse)
      .then(r => r.json()
        .then(r => r as Citation)));

  let cited = useQuery(`citation.${citationId}.cited`,
    async () =>
      fetch(`/api/citations/${citationId}/cited`).then(okResponse).then(r => r.json().then(e => e as Array<Citation>)))

  let cites = useQuery(`citation.${citationId}.cites`,
    async () =>
      fetch(`/api/citations/${citationId}/cites`).then(okResponse).then(r => r.json().then(e => e as Array<Citation>)))

  if (citationQuery.error || citationQuery.isError) {
    return <div className='container'>
      <div className="alert alert-danger d-flex align-items-center" role="alert">
        {String(citationQuery.error)}
      </div>
    </div>
  }

  if (citationQuery.isLoading)
    return <div className='container'>
      <p>loading</p>
    </div>

  return <div className='container'>
    <h1>{citationQuery.data.name}</h1>
    {citationQuery.data.description
      ? <blockquote className='blockquote'>{citationQuery.data.description}</blockquote>
      : null
    }
    <div className='fw-bold me-3 mb-1 d-inline-block'>Created</div>
    <span>{formatDate(new Date(citationQuery.data.created_at))}</span>
    <div></div>
    <div className='fw-bold me-3 mb-1 d-inline-block'>Updated</div>
    <span>{formatDate(new Date(citationQuery.data.updated_at))}</span>
    <hr />
    <AddLinks queries={[cites, cited]} citation={citationQuery.data} />
    <hr />
    <h2>Cites:</h2>
    <p className='small text-muted'>This article is cites the following articles:</p>
    <LoadingListGroup result={cited} keyFn={k => k.id}
                      nodeFn={c => <CitationItem citation={c} />} />
    <hr />
    <h2>Cited by:</h2>
    <p className='small text-muted'>This article is cited by the following articles:</p>
    <LoadingListGroup result={cites} keyFn={k => k.id}
                      nodeFn={c => <CitationItem citation={c} />} />
  </div>
}

function CitationItem({ citation }: { citation: Citation }) {
  return <>
    <Link to={'/citations/' + citation.id}>{citation.name}</Link>
    {' '}
    <span
      className='small text-muted'>(Created: {formatDate(new Date(citation.created_at))})</span>
  </>
}

function CitationResultItem({ citation }: { citation: Citation }) {
  return <>
    {citation.name}
    {' '}
    <span
      className='small text-muted'>(Created: {formatDate(new Date(citation.created_at))})</span>
  </>
}

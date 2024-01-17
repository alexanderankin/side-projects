/*
  View to show a single article ("citation")
*/
import { Link, useParams } from "react-router-dom";
import { useQuery } from "react-query";
import { formatDate } from "./dateFormatUtils";
import { LoadingListGroup, okResponse } from "./reusable";
import { Citation } from "./models";

export function CitationPage() {
  let { citationId } = useParams();

  let citationQuery = useQuery(`citation.${citationId}`, async () =>
    fetch(`/api/citations/${citationId}`).then(okResponse)
      .then(r => r.json())
  );

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
    <h2>Cites:</h2>
    <p className='small text-muted'>This article is cites the following articles:</p>
    <LoadingListGroup result={cited} keyFn={k => k.id} nodeFn={c => <CitationItem citation={c} />} />
    <hr />
    <h2>Cited by:</h2>
    <p className='small text-muted'>This article is cited by the following articles:</p>
    <LoadingListGroup result={cites} keyFn={k => k.id} nodeFn={c => <CitationItem citation={c} />} />
  </div>
}

function CitationItem({ citation }: { citation: Citation }) {
  return <>
    <Link to={'/citations/' + citation.id}>{citation.name}</Link>
    {' '}
    <span className='small text-muted'>(Created: {formatDate(new Date(citation.created_at))})</span>
  </>
}

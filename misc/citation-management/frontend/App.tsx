import { useQuery, UseQueryResult } from "react-query";
import { formatDate } from "./dateFormatUtils";
import { Link } from "react-router-dom";
import { AddMoreCitations, Header, LoadingListGroup, okResponse } from "./reusable";
import { Key, ReactNode } from "react";

function LatestCitationsJumbotron() {
  return <div className="px-5 py-5 mb-4 bg-light rounded-3">
    <div className="container-fluid py-2">
      <h1 className="display-5 fw-bold">Latest Citation Links</h1>
      <p className="col-md-8 fs-4">
        Here are the latest citation links that have been added:
      </p>
      <AddMoreCitations />
    </div>
  </div>;
}

function useLatestCitationsQuery() {
  return useQuery('latest', async () =>
    fetch('/api/latest-citations').then(okResponse)
      .then(r => r.json())
      .then(r => r.map(r => ({ ...r, createdAt: formatDate(new Date(r.createdAt)) }))));
}

function LatestCitationsLinkList() {
  let latest = useLatestCitationsQuery()

  if (latest.isError) {
    return <>
      <div className="alert alert-danger" role="alert">
        <div>
          {String(latest.error)}
        </div>
      </div>
    </>
  }

  if (latest.isLoading) {
    return <>
      <div className="list-group placeholder-glow">
        <a href="#" className="placeholder list-group-item list-group-item-action"></a>
        <a href="#" className="placeholder list-group-item list-group-item-action"></a>
        <a href="#" className="placeholder list-group-item list-group-item-action"></a>
        <a href="#" className="placeholder list-group-item list-group-item-action"></a>
      </div>
    </>
  }

  return <>
    <div className="list-group">
      {latest.data.map(e => {
        return <div key={e.from.id + '->' + e.to.id}
                    className="list-group-item list-group-item-action">
          From <Link to={`/citations/${e.from.id}`}>{e.from.name}</Link>
          {' '} to <Link to={`/citations/${e.to.id}`}>{e.to.name}</Link>
          {' '} on {e.createdAt}
        </div>
      })}
    </div>
  </>
}

export function App() {
  return <>
    <Header />
    <div className='container'>
      <div className='row'>
        <div className='col'><LatestCitationsJumbotron /></div>
        <div className='col'><LatestCitationsLinkList /></div>
      </div>
      <hr className='mt-5' />
      <div className='row'>.
        <div className='col'><h3>Latest Citations</h3></div>
        <div className='col'>
        </div>
      </div>
    </div>
  </>
}

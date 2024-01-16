import { useQuery } from "react-query";
import { formatDate } from "./dateFormatUtils";
import { Link, Outlet, useLocation, useRouteError } from "react-router-dom";

function Header() {
  return <header
    className="d-flex flex-wrap justify-content-center py-3 mb-4 border-bottom"
  >
    <Link to="/"
          className="d-flex align-items-center mb-3 mb-md-0 me-md-auto text-dark text-decoration-none">
      <span className="fs-4">Citation MGMT</span>
    </Link>

    <ul className="nav nav-pills" style={{ paddingLeft: 40 }}>
      <li className="nav-item"><a href="#" className="nav-link active"
                                  aria-current="page">Citations</a></li>
      <li className="nav-item"><a href="#" className="nav-link">Links</a>
      </li>
      {/* <li className="nav-item"><a href="#" className="nav-link">Pricing</a></li> */}
      {/* <li className="nav-item"><a href="#" className="nav-link">FAQs</a></li> */}
      {/* <li className="nav-item"><a href="#" className="nav-link">About</a></li> */}
    </ul>
  </header>;
}

function LatestCitationsJumbotron() {
  return <div className="px-5 py-5 mb-4 bg-light rounded-3">
    <div className="container-fluid py-2">
      <h1 className="display-5 fw-bold">Latest Citations</h1>
      <p className="col-md-8 fs-4">
        Here are the latest citation links that have been added:
      </p>
      <button className="btn btn-outline-success btn-lg" type="button">
        Add More
      </button>
    </div>
  </div>;
}

function LatestCitationsList() {
  let latest = useQuery('latest', async () =>
    fetch('/api/latest-citations').then(okResponse)
      .then(r => r.json())
      .then(r => r.map(r => ({ ...r, createdAt: formatDate(new Date(r.createdAt)) }))))

  if (latest.isError) {
    return <>
      <div className="alert alert-danger d-flex align-items-center" role="alert">
        <svg xmlns="http://www.w3.org/2000/svg"
             className="bi bi-exclamation-triangle-fill flex-shrink-0 me-2"
             viewBox="0 0 16 16" role="img" aria-label="Warning:">
          <path
            d="M8.982 1.566a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 5.995A.905.905 0 0 1 8 5zm.002 6a1 1 0 1 1 0 2 1 1 0 0 1 0-2z" />
        </svg>
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

  console.log('actually rendering', latest.data);
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

export async function okResponse(response: Response) {
  if (!response.ok) throw new Error('not okay response: ' + await response.text());
  return response;
}

export function ErrorPage() {
  const error = useRouteError();
  console.error(error);

  return <>
    <div className='container'>
      <Header />
    </div>
    <div className='container'>
      <div className='row'>
        <div id="error-page">
          <h1>Oops!</h1>
          <p>Sorry, an unexpected error has occurred.</p>
          <p>
            <i>{error['statusText'] || error['message']}</i>
          </p>
        </div>
      </div>
    </div>
  </>;
}

export function App() {
  let { pathname } = useLocation();
  return <>
    <div className='container'>
      <Header />
    </div>
    <div className='container'>
      <div className='row'>
        {pathname === "/"
          ? <>
            <div className='col'><LatestCitationsJumbotron /></div>
            <div className='col'><LatestCitationsList /></div>
          </>
          : <div className='col'><Outlet /></div>
        }
      </div>
    </div>
  </>
}

import { Link, useLocation, useRouteError } from "react-router-dom";
import { Key, ReactNode } from "react";
import { UseQueryResult } from "react-query";

/**
 * https://stackoverflow.com/a/62010324
 */
export function formDataObject(formData: FormData) {
  let keys: string[] = [];
  formData.forEach((_, key) => keys.push(key));

  return Object.fromEntries(
    keys.map(key => {
      let getAll = formData.getAll(key);
      return [
        key,
        getAll.length > 1
          ? getAll
          : formData.get(key)
      ];
    })
  )
}

const pages = [
  { name: 'Citations', url: '/citations' },
  { name: 'Links', url: '/links' },
]

export function Header() {
  let location = useLocation();
  return <div className='container'>
    <header
      className="d-flex flex-wrap justify-content-center py-3 mb-4 border-bottom"
    >
      <Link to="/"
            className="d-flex align-items-center mb-3 mb-md-0 me-md-auto text-dark text-decoration-none">
        <span className="fs-4">Citation MGMT</span>
      </Link>

      <ul className="nav nav-pills" style={{ paddingLeft: 40 }}>
        {pages.map(p => {
          let match = location.pathname.startsWith(p.url);
          return <li key={p.name} className='nav-item'>
            <Link to={p.url} className={'nav-link ' + (match ? 'active' : '')}
                  aria-current={match ? 'page' : false}>
              {p.name}
            </Link>
          </li>;
        })}
        {/*
        <li className="nav-item">
          <a href="#"
             className="nav-link active"
             aria-current="page">
            Citations
          </a>
        </li>
        <li className="nav-item">
          <a href="#" className="nav-link">
            Links
          </a>
        </li>
        */}

        {/* <li className="nav-item"><a href="#" className="nav-link">Pricing</a></li> */}
        {/* <li className="nav-item"><a href="#" className="nav-link">FAQs</a></li> */}
        {/* <li className="nav-item"><a href="#" className="nav-link">About</a></li> */}
      </ul>
    </header>
  </div>
}

export function ErrorPage() {
  const error = useRouteError();
  console.error(error);

  return <>
    <Header />
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

export async function okResponse(response: Response) {
  if (!response.ok) throw new Error('not okay response: ' + await response.text());
  return response;
}

export function AddMoreCitations() {
  return <Link to='/new-citation'>
    <button className="btn btn-outline-success btn-lg" type="button">
      Add More
    </button>
  </Link>;
}

export function JumboTron({ title, children }: {
  title?: string,
  children?: ReactNode
}) {
  return <>
    <div className="px-5 py-5 mb-4 bg-light rounded-3">
      <div className="container-fluid py-2">
        {!title ? null : <h1 className="display-5 fw-bold">{title}</h1>}
        {children || null}
      </div>
    </div>
  </>
}

export function LoadingListGroup<T>(
  {
    result,
    keyFn,
    nodeFn,
  }: {
    result: UseQueryResult<Array<T>, any>,
    keyFn: (t: T) => Key,
    nodeFn: (t: T) => ReactNode,
  }) {

  if (result.isError)
    return <div className="alert alert-danger" role="alert">
      <div>
        {String(result.error)}
      </div>
    </div>

  if (result.isLoading)
    return <div className="list-group placeholder-glow">
      <a href="#" className="placeholder list-group-item list-group-item-action" style={{height: '2em' }}><span>{' '}</span></a>
      <a href="#" className="placeholder list-group-item list-group-item-action" style={{height: '2em' }}><span>{' '}</span></a>
      <a href="#" className="placeholder list-group-item list-group-item-action" style={{height: '2em' }}><span>{' '}</span></a>
      <a href="#" className="placeholder list-group-item list-group-item-action" style={{height: '2em' }}><span>{' '}</span></a>
    </div>

  return <div className="list-group">
    {result.data.map((item) => {
      return <div key={keyFn(item)}
                  className="list-group-item list-group-item-action">
        {nodeFn(item)}
      </div>
    })}
  </div>
}

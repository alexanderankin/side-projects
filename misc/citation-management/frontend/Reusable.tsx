import { Link, useRouteError } from "react-router-dom";

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

export function Header() {
  return <div className='container'>
    <header
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
